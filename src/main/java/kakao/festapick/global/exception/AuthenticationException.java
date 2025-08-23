package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public AuthenticationException(ExceptionCode exceptionCode) {
        super(exceptionCode.getErrorMessage());
        this.exceptionCode = exceptionCode;
    }
}
