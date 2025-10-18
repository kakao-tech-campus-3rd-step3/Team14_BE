package kakao.festapick.redis.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kakao.festapick.chat.dto.ChatPayload;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
@SpringBootTest
public class RedisConfigTest {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("testObjectKey");
    }

    @Test
    void testChatPayloadSerialization() {
        ChatPayload payload = new ChatPayload(1L, 1L, "test", "test", "test", "test", LocalDateTime.now());

        redisTemplate.opsForValue().set("testObjectKey", payload);
        Object value = redisTemplate.opsForValue().get("testObjectKey");

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        ChatPayload result = objectMapper.convertValue(value, ChatPayload.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.userId()).isEqualTo(payload.userId());
            softly.assertThat(result.content()).isEqualTo(payload.content());
            softly.assertThat(result.profileImgUrl()).isEqualTo(payload.profileImgUrl());
            softly.assertThat(result.createdDate()).isEqualTo(payload.createdDate());
        });
    }
}
