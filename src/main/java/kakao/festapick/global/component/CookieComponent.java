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

    public String createRefreshToken(String value) {
        return ResponseCookie.from("refreshToken", value)
                .path("/")
                .secure(secure)
                .maxAge(604800)
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
}
