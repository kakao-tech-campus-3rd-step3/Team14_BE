package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public ExternalApiException(ExceptionCode exceptionCode) {
        super(exceptionCode.getErrorMessage());
        this.exceptionCode = exceptionCode;
    }
}
