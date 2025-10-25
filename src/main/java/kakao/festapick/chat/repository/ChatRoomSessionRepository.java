package kakao.festapick.chat.repository;

import kakao.festapick.chat.domain.ChatRoomSession;
import org.springframework.data.repository.CrudRepository;

public interface ChatRoomSessionRepository extends CrudRepository<ChatRoomSession, String> {

}
