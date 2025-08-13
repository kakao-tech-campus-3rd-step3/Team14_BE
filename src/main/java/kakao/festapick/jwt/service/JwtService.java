package kakao.festapick.jwt.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.domain.RefreshToken;
import kakao.festapick.jwt.repository.RefreshTokenRepository;
import kakao.festapick.user.domain.User;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2UserService userService;
    private final JWTUtil jwtUtil;

    public RefreshToken saveRefreshToken(String Identifier, String refreshToken) {

        User findUser = userService.findByIdentifier(Identifier);

        return refreshTokenRepository.save(new RefreshToken(findUser, refreshToken));
    }

    public void exchangeToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) throw new AuthenticationException("쿠키가 존재하지 않습니다.");

        Cookie refreshCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equalsIgnoreCase("refreshToken"))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("리프래시 토큰이 존재하지 않습니다."));

        String oldRefreshToken = refreshCookie.getValue();

        if (!jwtUtil.validateToken(oldRefreshToken, false))
            throw new AuthenticationException("리프래시 토큰이 아닙니다.");
        Claims claims = jwtUtil.getClaims(oldRefreshToken);
        String identifier = claims.get("identifier").toString();
        String role = claims.get("role").toString();

        User findUser = userService.findByIdentifier(identifier);

        String newAccessToken = jwtUtil.createJWT(identifier, role, true);
        String newRefreshToken = jwtUtil.createJWT(identifier, role, false);

        refreshTokenRepository.deleteByUser(findUser);
        refreshTokenRepository.save(new RefreshToken(findUser,newRefreshToken));

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        response.addHeader("Set-Cookie", createCookie("refreshToken", newRefreshToken));

    }

    private String createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .path("/")
                .secure(false)
                .maxAge(604800)
                .httpOnly(true)
                .build()
                .toString();
    }
}
