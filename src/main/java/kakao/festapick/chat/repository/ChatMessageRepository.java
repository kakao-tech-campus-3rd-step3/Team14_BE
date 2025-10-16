package kakao.festapick.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(value = "select c from ChatMessage c join fetch c.user u where c.chatRoom.id = :chatRoomId")
    List<ChatMessage> findAllByChatRoomId(Long chatRoomId);

    @Query(value = "select c from ChatMessage c where c.user.id = :userId")
    List<ChatMessage> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatMessage c where c.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(Long chatRoomId);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatMessage c where c.user.id = :userId")
    void deleteByUserId(Long userId);
}
