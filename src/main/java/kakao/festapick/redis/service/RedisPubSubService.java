package kakao.festapick.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRequestDto;
import kakao.festapick.chat.dto.UnreadEventPayload;
import kakao.festapick.chat.service.ChatMessageLowService;
import kakao.festapick.chat.service.ChatParticipantLowService;
import kakao.festapick.chat.service.ChatRoomLowService;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.global.exception.JsonParsingException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RedisPubSubService implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate webSocket;
    private final UserLowService userLowService;
    private final ChatMessageLowService chatMessageLowService;
    private final ChatRoomLowService chatRoomLowService;
    private final ChatParticipantLowService chatParticipantLowService;
    private final TemporalFileRepository temporalFileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 채팅 메시지 보내기
    public void sendChatMessageToRedis(Long chatRoomId, ChatRequestDto requestDto, Long userId) {
        UserEntity sender = userLowService.getReferenceById(userId);
        ChatRoom chatRoom = chatRoomLowService.findByRoomId(chatRoomId);
        String imageUrl = null;

        if (requestDto.imageInfo() != null) {
            imageUrl = requestDto.getImageUrl();
            Long temporalFileId = requestDto.getTemporalFileId();
            // 저장이 정상적으로 됬을 경우 임시 파일 목록에서 제거
            temporalFileRepository.deleteByIds(List.of(temporalFileId));
        }

        ChatMessage chatMessage = new ChatMessage(requestDto.content(), imageUrl, chatRoom, sender);
        // db에 저장 후
        ChatMessage savedMessage = chatMessageLowService.save(chatMessage);
        ChatPayload payload = new ChatPayload(savedMessage);

        // 채팅방의 버전 변경
        chatRoom.updateVersion();

        // redis로 채팅 메시지 브로드캐스트
        redisTemplate.convertAndSend("chat." + chatRoom.getId(), payload);

        // 자신을 제외한 채팅방 참여자들의 id
        List<Long> participantsUserIdList = chatParticipantLowService.findByChatRoomId(
                        chatRoom.getId())
                .stream()
                .map(chatParticipant -> chatParticipant.getUser().getId())
                .filter(id -> !id.equals(userId))
                .toList();

        UnreadEventPayload event = new UnreadEventPayload(chatRoom.getId(), participantsUserIdList);

        // redis로 각 인스턴스에 브로드캐스트
        redisTemplate.convertAndSend("unreads", event);

        // 자신이 보낸 채팅 읽음 처리
        ChatParticipant participant = chatParticipantLowService.findByChatRoomIdAndUserId(
                chatRoomId, userId);
        participant.syncVersion();
    }

    // 인스턴스가 redis pubsub으로 메시지 받으면
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        // 채팅 전송의 경우
        if (channel.startsWith("chat.")) {
            handleChatMessage(channel, body);
        }
        // 새로운 채팅 알람인 경우
        else if (channel.equals("unreads")) {
            handleUnread(body);
        }
    }

    private void handleChatMessage(String channel, String body) {
        try {
            // channel의 경우 chat.XXX로 . 뒤는 양수 숫자 (. 다음 첫 문자 1 - 9, 첫 문자가 아니면  0 - 9 가능)만 존재해야 한다
            Long chatRoomId = Long.valueOf(channel.replaceAll("\\D+", ""));
            ChatPayload payload = objectMapper.readValue(body, ChatPayload.class);
            // subscribe한 클라이언트로 전파
            webSocket.convertAndSend("/sub/" + chatRoomId + "/messages", payload);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("redis payload 파싱 실패");
        }
    }

    private void handleUnread(String body) {
        try {
            UnreadEventPayload event = objectMapper.readValue(body, UnreadEventPayload.class);
            // 채팅방의 모든 참여자에게 전송 시도
            for (Long userId : event.userIds()) {
                webSocket.convertAndSendToUser(
                        userId.toString(),
                        "/queue/unreads",
                        Map.of("chatRoomId", event.chatRoomId())
                );
            }
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("redis payload 파싱 실패");
        }
    }
}


