package kakao.festapick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotFoundEntityException extends RuntimeException {

    private final ExceptionCode exceptionCode;

}
