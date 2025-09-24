package kakao.festapick.chat.repository;

import java.util.Optional;
import kakao.festapick.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = "select c from ChatRoom c where c.festival.id = :festivalId")
    Optional<ChatRoom> findByFestivalId(Long festivalId);

    @Query(value = "select c from ChatRoom c where c.id = :roomId")
    Optional<ChatRoom> findByRoomId(Long roomId);
}
