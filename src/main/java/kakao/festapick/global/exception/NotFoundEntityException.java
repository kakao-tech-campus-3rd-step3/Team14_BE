package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class NotFoundEntityException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public NotFoundEntityException(ExceptionCode exceptionCode) {
        super(exceptionCode.getErrorMessage());
        this.exceptionCode = exceptionCode;
    }
}
