package kakao.festapick.chat.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.user.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    @Query(value = "select (count(cp) > 0) from ChatParticipant cp where cp.user= :user and cp.chatRoom= :chatRoom")
    boolean existsByUserAndChatRoom(UserEntity user, ChatRoom chatRoom);

    @Query(value = "select cp from ChatParticipant cp join fetch cp.chatRoom cr join fetch cr.festival f where cp.chatRoom.id = :chatRoomId and cp.user.id = :userId")
    Optional<ChatParticipant> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    @Query(value = "select c from ChatParticipant c join fetch c.user u where c.chatRoom.id = :chatRooomId")
    List<ChatParticipant> findByChatRoomId(Long chatRooomId);

    @Query(value = "select cp from ChatParticipant cp join fetch cp.chatRoom cr join fetch cr.festival f where cp.user.id = :userId",
            countQuery = "select count(cp) from ChatParticipant cp where cp.user.id = :userId")
    Page<ChatParticipant> findByUserIdWithChatRoom(Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatParticipant c where c.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(Long chatRoomId);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatParticipant c where c.user.id = :userId")
    void deleteByUserId(Long userId);

}
