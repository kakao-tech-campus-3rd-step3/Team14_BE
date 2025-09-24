package kakao.festapick.chat.service;

import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserLowService userLowService;

    public void enterChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.CHATROOM_NOT_FOUND));

        UserEntity user = userLowService.findById(userId);

        if (!chatParticipantRepository.existsByUserAndChatRoom(user, chatRoom)) {
            ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);
            chatParticipantRepository.save(chatParticipant);
        }
    }
}
