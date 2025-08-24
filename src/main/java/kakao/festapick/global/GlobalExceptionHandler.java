package kakao.festapick.global;

import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.NotFoundEntityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String,String>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getExceptionCode().getErrorMessage()));
    }

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<Map<String,String>> handleNotFoundEntityException(NotFoundEntityException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getExceptionCode().getErrorMessage()));
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Map<String,String>> handleDuplicateEntityException(DuplicateEntityException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getExceptionCode().getErrorMessage()));
    }
}
