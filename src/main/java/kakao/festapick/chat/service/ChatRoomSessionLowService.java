package kakao.festapick.chat.service;

import java.util.Optional;
import kakao.festapick.chat.dto.ChatRoomSessionStatusDto;
import kakao.festapick.chat.dto.ReadEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ChatRoomSessionLowService {

    private final RedisTemplate<String,Object> redisTemplate;

    // userId, chatRoomId인 chatroom session을 추가, 없으면 해시 생성 후 저장
    public void addChatRoomSession(Long roomId, Long userId, String sessionId) {
        redisTemplate.opsForValue().set(getSessionKey(sessionId), roomId + ":" + userId);
        redisTemplate.opsForHash().put(getSubscribeKey(roomId, userId), sessionId, 1);
        redisTemplate.expire(getSubscribeKey(roomId, userId), Duration.ofHours(1));
        redisTemplate.expire(getSessionKey(sessionId), Duration.ofHours(1));
        // 같은 사람의 다른 세션에도 알리기 위해 전송
        ReadEventPayload event = new ReadEventPayload(roomId, userId);
        redisTemplate.convertAndSend("reads", event);
    }

    // userId, chatRoomId인 해시의 chatroom session 제거
    public void removeChatRoomSession(Long roomId, Long userId, String sessionId) {
        redisTemplate.opsForHash().delete(getSubscribeKey(roomId, userId), sessionId);
        redisTemplate.delete(getSessionKey(sessionId));
    }

    // 하트 비트 받으면 키 유효기간 갱신
    public void refreshTtl(String sessionId) {
        String value = (String) redisTemplate.opsForValue().get(getSessionKey(sessionId));
        if(value != null) {
            String[] split = value.split(":");
            Long roomId = Long.parseLong(split[0]);
            Long userId = Long.parseLong(split[1]);

            redisTemplate.expire(getSubscribeKey(roomId, userId), Duration.ofHours(1));
            redisTemplate.expire(getSessionKey(sessionId), Duration.ofHours(1));
        }
    }

    public Optional<ChatRoomSessionStatusDto> deleteBySessionId(String sessionId) {
        String value = (String) redisTemplate.opsForValue().get(getSessionKey(sessionId));
        if(value != null) {
            String[] split = value.split(":");
            Long roomId = Long.parseLong(split[0]);
            Long userId = Long.parseLong(split[1]);

            redisTemplate.opsForHash().delete(getSubscribeKey(roomId, userId), sessionId);
            redisTemplate.delete(getSessionKey(sessionId));
            return Optional.of(new ChatRoomSessionStatusDto(roomId, userId));
        }
        return Optional.empty();
    }

    // chatroomsession이 존재하지 않거나 count가 0이라면 false를 반환한다.
    public boolean existsById(Long roomId, Long userId) {
        Long count = redisTemplate.opsForHash().size(getSubscribeKey(roomId, userId));
        return count > 0;
    }

    private String getSubscribeKey(Long roomId, Long userId) {
        return "chatRoomSession:" + roomId + ":" + userId;
    }

    private String getSessionKey(String sessionId) {
        return "chatRoomSession:" + sessionId;
    }
}
