package kakao.festapick.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.service.ChatMessageService;
import kakao.festapick.global.exception.JsonParsingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscribeService implements MessageListener{

    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        Long chatRoomId = Long.valueOf(channel.replaceAll("\\D+", ""));
        try {
            ChatPayload payload = objectMapper.readValue(body, ChatPayload.class);
            chatMessageService.sendChatToClient(chatRoomId, payload);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("redis payload 파싱 실패");
        }
    }
}


