package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class WebSocketException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public WebSocketException(ExceptionCode exceptionCode) {
        super(exceptionCode.getErrorMessage());
        this.exceptionCode = exceptionCode;
    }
}
