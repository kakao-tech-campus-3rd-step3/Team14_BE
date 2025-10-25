package kakao.festapick.global;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kakao.festapick.global.exception.WebSocketException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

@ControllerAdvice
public class StompExceptionHandler {

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public Map<String, List<Map<String,String>>> handleStompValidationException(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream().map(error -> Map.of(error.getField(), Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value")))
                .toList();

        List<Map<String, String>> globalErrors = ex.getBindingResult().getGlobalErrors()
                .stream().map(error -> Map.of("message", Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value")))
                .toList();
        return Map.of("globalErrors", globalErrors, "fieldErrors", fieldErrors);
    }

    @MessageExceptionHandler(WebSocketException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public Map<String,String> handleWebSocketException(WebSocketException e) {
        return Map.of("message", e.getExceptionCode().getErrorMessage());
    }

}
