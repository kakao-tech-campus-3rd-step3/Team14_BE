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
    public ChatParticipant enterChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomLowService.findByRoomId(roomId);
        UserEntity user = userLowService.getReferenceById(userId);

        return chatParticipantLowService.existsByUserAndChatRoom(user, chatRoom) ?
                chatParticipantLowService.findByChatRoomIdAndUserIdWithChatRoom(roomId, userId)
                : chatParticipantLowService.save(new ChatParticipant(user, chatRoom));
    }

    // 채팅방에서 나가기 (Chat Participant에서 삭제해 더이상 알림 / 목록에서 보이지 않음)
    public void exitChatRoom(Long userId, Long chatRoomId) {
        chatParticipantLowService.deleteByChatRoomIdAndUserId(chatRoomId, userId);
    }

    // 내가 접속했던 채팅방들의 정보 조회
    public Page<ChatRoomReadStatusDto> getMyChatRoomsReadStatus(Long userId, Pageable pageable) {
        Page<ChatParticipant> chatParticipants = chatParticipantLowService.findByUserIdWithChatRoomAndFestival(
                userId, pageable);
        return chatParticipants.map(this::getMyChatRoomReadStatus);
    }

    private ChatRoomReadStatusDto getMyChatRoomReadStatus(ChatParticipant chatParticipant) {
        ChatRoom chatRoom = chatParticipant.getChatRoom();
        Festival festival = chatRoom.getFestival();

        // 채팅방의 버전이 채팅 참여자의 버전과 다른 경우 새 메시지 있다 판단
        Boolean hasNewMessage = !(chatRoom.getMessageSeq().equals(chatParticipant.getMessageSeq()));

        return new ChatRoomReadStatusDto(
                chatRoom.getId(), chatRoom.getRoomName(), festival.getId(),
                festival.getPosterInfo(), hasNewMessage
        );
    }
}
