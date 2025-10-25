package kakao.festapick.chat.domain;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;


@Getter
@NoArgsConstructor
@RedisHash(value = "chatRoomSession", timeToLive = 3600) // 1시간
public class ChatRoomSession {

    @Id
    private String id; // "roomId:userId"

    public ChatRoomSession(Long roomId, Long userId) {
        this.id = roomId + ":" + userId;
    }
}

