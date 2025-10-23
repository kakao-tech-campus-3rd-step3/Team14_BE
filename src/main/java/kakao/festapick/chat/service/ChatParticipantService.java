package kakao.festapick.chat.service;

import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomReadStatusDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatParticipantService {

    private final ChatParticipantLowService chatParticipantLowService;
    private final ChatRoomLowService chatRoomLowService;
    private final UserLowService userLowService;

    //채팅룸 입장 시 Chat Participant에 저장
    public void enterChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomLowService.findByRoomId(roomId);

        UserEntity user = userLowService.getReferenceById(userId);

        if (!chatParticipantLowService.existsByUserAndChatRoom(user, chatRoom)) {
            ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);
            chatParticipantLowService.save(chatParticipant);
        }
    }

    // 내가 접속했던 채팅방들의 정보 조회
    public Page<ChatRoomReadStatusDto> getMyChatRoomsReadStatus(Long userId, Pageable pageable) {
        Page<ChatParticipant> chatParticipants = chatParticipantLowService.findByUserIdWithChatRoom(
                userId, pageable);
        return chatParticipants.map(this::getMyChatRoomReadStatus);
    }

    // 채팅방 읽음 확인
    public void readChatRoomMessage(Long chatRoomId, Long userId) {
        ChatParticipant chatParticipant = chatParticipantLowService.findByChatRoomIdAndUserId(chatRoomId, userId);
        chatParticipant.syncVersion();
    }

    private ChatRoomReadStatusDto getMyChatRoomReadStatus(ChatParticipant chatParticipant) {
        ChatRoom chatRoom = chatParticipant.getChatRoom();
        Festival festival = chatRoom.getFestival();

        // 채팅방의 버전이 채팅 참여자의 버전과 다른 경우 새 메시지 있다 판단
        Boolean hasNewMessage = !(chatRoom.getVersion().equals(chatParticipant.getVersion()));

        return new ChatRoomReadStatusDto(
                chatRoom.getId(), chatRoom.getRoomName(), festival.getId(),
                festival.getPosterInfo(), hasNewMessage
        );
    }
}
