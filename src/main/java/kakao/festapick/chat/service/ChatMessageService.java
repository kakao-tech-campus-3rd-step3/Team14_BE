package kakao.festapick.chat.service;

import java.util.List;
import java.util.Objects;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRequestDto;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final SimpMessagingTemplate webSocket;
    private final UserLowService userLowService;
    private final ChatMessageLowService chatMessageLowService;
    private final ChatRoomLowService chatRoomLowService;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 채팅 메시지 보내기
    public void sendChatToRedis(Long chatRoomId, ChatRequestDto requestDto, Long userId) {
        UserEntity sender = userLowService.findById(userId);
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

        //마지막에 redis로 전파
        redisTemplate.convertAndSend("chat." + chatRoom.getId(), payload);
    }

    public void sendChatToClient(Long chatRoomId, ChatPayload payload) {
        webSocket.convertAndSend("/sub/" + chatRoomId + "/messages", payload);
    }

    // 채팅방에서 최근 메시지 조회 (Pageable)
    public Page<ChatPayload> getPreviousMessages(Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> previousMessages = chatMessageLowService.findByChatRoomId(chatRoomId,
                pageable);
        return previousMessages.map(ChatPayload::new);
    }

    // 유저가 작성한 메시지 전체 삭제 기능
    public void deleteChatMessagesByUserId(Long userId) {
        List<String> chatMessageImageUrls = chatMessageLowService
                .findAllByUserId(userId)
                .stream()
                .map(ChatMessage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();

        chatMessageLowService.deleteByUserId(userId);
        s3Service.deleteFiles(chatMessageImageUrls); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }
}
