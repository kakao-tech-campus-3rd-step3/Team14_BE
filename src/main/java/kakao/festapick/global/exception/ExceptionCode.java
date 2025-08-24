package kakao.festapick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    //Authentication
    COOKIE_NOT_EXIST("쿠키가 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_EXIST("리프레시 토큰이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN("리프레시 토큰이 유효하지 않습니다."),

    //NotFound
    FESTIVAL_NOT_FOUND("존재하지 않는 축제입니다."),
    USER_NOT_FOUND("존재하지 않는 회원입니다."),
    WISH_NOT_FOUND("존재하지 않는 좋아요입니다."),
    REVIEW_NOT_FOUND("존재하지 않는 리뷰입니다."),

    //CONFLICT
    WISH_DUPLICATE("이미 좋아요한 축제입니다."),
    REVIEW_DUPLICATE("이미 리뷰를 한 축제입니다, 리뷰 수정을 이용해주세요.");

    private final String errorMessage;

}
