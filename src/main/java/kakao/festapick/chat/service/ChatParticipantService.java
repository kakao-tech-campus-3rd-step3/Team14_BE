package kakao.festapick.chat.service;

import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
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

        UserEntity user = userLowService.findById(userId);

        if (!chatParticipantLowService.existsByUserAndChatRoom(user, chatRoom)) {
            ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);
            chatParticipantLowService.save(chatParticipant);
        }
    }
}
