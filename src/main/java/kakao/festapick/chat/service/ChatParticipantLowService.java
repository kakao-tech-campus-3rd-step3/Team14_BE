package kakao.festapick.chat.service;

import java.util.List;
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
public class ChatParticipantLowService {

    private final ChatParticipantRepository chatParticipantRepository;

    public ChatParticipant save(ChatParticipant chatParticipant) {
        return chatParticipantRepository.save(chatParticipant);
    }

    public boolean existsByUserAndChatRoom(UserEntity user, ChatRoom chatRoom) {
        return chatParticipantRepository.existsByUserAndChatRoom(user, chatRoom);
    }

    public List<ChatParticipant> findByChatRoomId(Long chatRooomId) {
        return chatParticipantRepository.findByChatRoomId(chatRooomId);
    }

    public void deleteByChatRoomId(Long chatRoomId) {
        chatParticipantRepository.deleteByChatRoomId(chatRoomId);
    }

    public void deleteByUserId(Long userId) {
        chatParticipantRepository.deleteByUserId(userId);
    }
}
