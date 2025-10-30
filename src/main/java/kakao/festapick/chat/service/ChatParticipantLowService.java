package kakao.festapick.chat.service;

import java.util.List;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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

    public ChatParticipant findByChatRoomIdAndUserIdWithChatRoom(Long chatRoomId, Long userId) {
        return chatParticipantRepository.findByChatRoomIdAndUserIdWithChatRoom(chatRoomId, userId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.CHAT_PARTICIPANT_NOT_FOUND));
    }

    public List<ChatParticipant> findByChatRoomId(Long chatRooomId) {
        return chatParticipantRepository.findByChatRoomId(chatRooomId);
    }

    public Page<ChatParticipant> findByUserIdWithChatRoomAndFestival(Long userId, Pageable pageable) {
        return chatParticipantRepository.findByUserIdWithChatRoomAndFestival(userId, pageable);
    }

    public void deleteByChatRoomIdAndUserId(Long chatRoomId, Long userId) {
        chatParticipantRepository.deleteByChatRoomIdAndUserId(chatRoomId, userId);
    }

    public void deleteByChatRoomId(Long chatRoomId) {
        chatParticipantRepository.deleteByChatRoomId(chatRoomId);
    }

    public void deleteByUserId(Long userId) {
        chatParticipantRepository.deleteByUserId(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void syncMessageSeq(Long userId, Long chatRoomId, Long newMessageSeq) {
        chatParticipantRepository.syncMessageSeq(userId, chatRoomId, newMessageSeq);
    }
}
