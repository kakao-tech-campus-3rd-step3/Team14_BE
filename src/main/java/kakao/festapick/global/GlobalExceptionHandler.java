package kakao.festapick.global;

import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.NotFoundEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownContentTypeException;

@Slf4j
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

    @ExceptionHandler(ResourceAccessException.class)
    public void handelRestClientTimeOutException(ResourceAccessException e){
        log.error("timeout 발생 : " + e.getMessage());
    }

//    @ExceptionHandler(UnknownContentTypeException.class)
//    public void handelRestClientLimitException(UnknownContentTypeException e){
//        log.error("1일 API 호출 횟수 초과");
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<Map<String,String>>>>  handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream().map(error -> Map.of(error.getField(), Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value")))
                .toList();

        List<Map<String, String>> globalErrors = ex.getBindingResult().getGlobalErrors()
                .stream().map(error -> Map.of("message", Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value")))
                .toList();


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("globalErrors", globalErrors, "fieldErrors", fieldErrors));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String,String>> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

}
