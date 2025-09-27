package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class TourApiException extends RuntimeException{

    private String errMsg;

    public TourApiException(String errMsg) {
        super(errMsg);
        this.errMsg = errMsg;
    }
}
