package kakao.festapick.global;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@RequiredArgsConstructor
@Component
@Slf4j
public class StompInterceptorExceptionHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage,
            Throwable ex) {
        String exceptionMessage = ex.getCause().getMessage();

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        headerAccessor.setMessage(exceptionMessage);
        headerAccessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(
                exceptionMessage.getBytes(StandardCharsets.UTF_8),
                headerAccessor.getMessageHeaders());

        log.info("인터셉터 예외 발생!:{}", message);
        return message;

    }
}
