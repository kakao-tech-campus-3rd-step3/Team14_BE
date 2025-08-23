package kakao.festapick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DuplicateEntityException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
