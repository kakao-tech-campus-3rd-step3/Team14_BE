package kakao.festapick.chat.repository;

import kakao.festapick.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(value = "select c from ChatMessage c join fetch c.user u where c.chatRoom.id = :chatRoomId",
    countQuery = "select count(c) from ChatMessage c where c.chatRoom.id = :chatRoomId")
    Page<ChatMessage> findByChatRoomId(Long chatRoomId, Pageable pageable);
}
