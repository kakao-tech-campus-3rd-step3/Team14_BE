package kakao.festapick.global.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieComponent {

    @Value("${spring.cookie.domain}")
    private String domain;
    @Value("${spring.cookie.secure}")
    private boolean secure;
    @Value("${spring.cookie.same}")
    private String same;

    private static final int accessTokenExpiresIn = 3600; // 1시간
    private static final int refreshTokenExpiresIn = 604800; // 7일;

    public String createRefreshToken(String value) {
        return ResponseCookie.from("refreshToken", value)
                .path("/")
                .secure(secure)
                .maxAge(refreshTokenExpiresIn)
                .domain(domain)
                .httpOnly(true)
                .sameSite(same)
                .build()
                .toString();
    }

    public String deleteRefreshToken() {
        return ResponseCookie.from("refreshToken", null)
                .path("/")
                .secure(secure)
                .maxAge(0)
                .domain(domain)
                .httpOnly(true)
                .sameSite(same)
                .build()
                .toString();
    }

    public String createAccessToken(String value) {
        return ResponseCookie.from("accessToken", value)
                .path("/")
                .secure(secure)
                .maxAge(accessTokenExpiresIn)
                .domain(domain)
                .httpOnly(true)
                .sameSite(same)
                .build()
                .toString();
    }

    public String deleteAccessToken() {
        return ResponseCookie.from("accessToken", null)
                .path("/")
                .secure(secure)
                .maxAge(0)
                .domain(domain)
                .httpOnly(true)
                .sameSite(same)
                .build()
                .toString();
    }
}
