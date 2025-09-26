package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class DuplicateEntityException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public DuplicateEntityException(ExceptionCode exceptionCode) {
        super(exceptionCode.getErrorMessage());
        this.exceptionCode = exceptionCode;
    }
}
