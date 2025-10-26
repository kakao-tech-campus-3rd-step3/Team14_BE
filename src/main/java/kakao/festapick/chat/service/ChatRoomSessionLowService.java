package kakao.festapick.chat.service;

import kakao.festapick.chat.dto.ReadEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatRoomSessionLowService {

    private final RedisTemplate<String,Object> redisTemplate;
    private static final String HASH_KEY = "count";

    // userId, chatRoomId인 chatroomsession의 count 증가, 없으면 해시 생성 후 count = 1로 설정
    public void increaseChatRoomSession(Long roomId, Long userId) {
        String key = "chatRoomSession:" + roomId + ":" + userId;
        redisTemplate.opsForHash().increment(key, HASH_KEY, 1L);
        redisTemplate.expire(key, Duration.ofHours(1));

        // 같은 사람의 다른 세션에도 알리기 위해 전송
        ReadEventPayload event = new ReadEventPayload(roomId, userId);
        redisTemplate.convertAndSend("reads", event);
    }

    // userId, chatRoomId인 chatroomsession의 count 감소
    public void decreaseChatRoomSession(Long roomId, Long userId) {
        String key = "chatRoomSession:" + roomId + ":" + userId;
        redisTemplate.opsForHash().increment(key, HASH_KEY, -1L);
    }

    // chatroomsession이 존재하지 않거나 count가 0이라면 false를 반환한다.
    public boolean existsById(Long roomId, Long userId) {
        String key = getKey(roomId, userId);
        Object count = redisTemplate.opsForHash().get(key, HASH_KEY);
        if (count == null) return false;

        long value;

        try {
            value = Long.parseLong(Objects.toString(count));
        } catch (NumberFormatException e) {
            return false;
        }
        return value > 0;
    }

    private String getKey(Long roomId, Long userId) {
        return "chatRoomSession:" + roomId + ":" + userId;
    }
}
