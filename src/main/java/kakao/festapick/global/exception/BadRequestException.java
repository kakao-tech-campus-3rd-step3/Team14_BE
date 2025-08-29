package kakao.festapick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BadRequestException extends RuntimeException {
  private final ExceptionCode exceptionCode;

  public BadRequestException(ExceptionCode exceptionCode, String errorMessage) {
    super(exceptionCode.getErrorMessage() + " : " + errorMessage);
    this.exceptionCode = exceptionCode;
  }
}
