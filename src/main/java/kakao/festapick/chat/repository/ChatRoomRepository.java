package kakao.festapick.chat.repository;

import java.util.Optional;
import kakao.festapick.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = "select c from ChatRoom c where c.festival.id = :festivalId")
    Optional<ChatRoom> findByFestivalId(Long festivalId);

    @Query(value = "select c from ChatRoom c where c.id = :roomId")
    Optional<ChatRoom> findByRoomId(Long roomId);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatRoom c where c.id = :chatRoomId")
    void deleteById(Long chatRoomId);
}
