package kakao.festapick.global;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

@Slf4j
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
        log.info(Map.of("globalErrors", globalErrors, "fieldErrors", fieldErrors).toString());
        return Map.of("globalErrors", globalErrors, "fieldErrors", fieldErrors);
    }

}
