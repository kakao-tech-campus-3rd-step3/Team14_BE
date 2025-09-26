package kakao.festapick.global.exception;

import lombok.Getter;

@Getter
public class JsonParsingException extends RuntimeException {
    private String errMsg;

    public JsonParsingException(String errMsg) {
        super(errMsg);
        this.errMsg = errMsg;
    }
}
