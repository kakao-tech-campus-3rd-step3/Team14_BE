package kakao.festapick.chat.service;

import kakao.festapick.chat.repository.ChatRoomSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ChatRoomSessionLowService {

    private final RedisTemplate<String,Object> redisTemplate;
    private final ChatRoomSessionRepository chatRoomSessionRepository;

    // userId, chatRoomId인 chatroomsession의 count 증가, 없으면 해시 생성 후 count = 1로 설정
    public void increaseChatRoomSession(Long roomId, Long userId) {
        String key = "chatRoomSession:" + roomId + ":" + userId;
        redisTemplate.opsForHash().increment(key, "count", 1L);
        redisTemplate.expire(key, Duration.ofHours(1));
    }

    // userId, chatRoomId인 chatroomsession의 count 감소, 0이면 해시 삭제
    public void decreaseChatRoomSession(Long roomId, Long userId) {
        String key = "chatRoomSession:" + roomId + ":" + userId;
        Long after = redisTemplate.opsForHash().increment(key, "count", -1L);
        if (after <= 0L) {
            redisTemplate.delete(key);
        }
        else {
            redisTemplate.expire(key, Duration.ofHours(1));
        }
    }

    public boolean existsById(String id) {
        return chatRoomSessionRepository.existsById(id);
    }
}
