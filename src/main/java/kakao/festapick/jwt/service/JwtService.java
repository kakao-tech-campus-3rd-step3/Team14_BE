package kakao.festapick.jwt.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.component.TokenEncoder;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.domain.RefreshToken;
import kakao.festapick.jwt.repository.RefreshTokenRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2UserService userService;
    private final JWTUtil jwtUtil;
    private final CookieComponent cookieComponent;
    private final TokenEncoder tokenEncoder;

    public RefreshToken saveRefreshToken(String identifier, String refreshToken) {

        UserEntity findUser = userService.findByIdentifier(identifier);

        String encodedRefreshToken = tokenEncoder.encode(refreshToken);

        refreshTokenRepository.deleteByUser(findUser);

        return refreshTokenRepository.save(new RefreshToken(findUser, encodedRefreshToken));
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
            throw new AuthenticationException("리프래시 토큰이 유효하지 않습니다.");
        Claims claims = jwtUtil.getClaims(oldRefreshToken);
        String identifier = claims.get("identifier").toString();
        String role = claims.get("role").toString();

        RefreshToken findRefreshToken = findByUserIdentifier(identifier);

        if(!tokenEncoder.match(findRefreshToken.getToken(), oldRefreshToken)) {
            log.info("token invalidated");
            throw new AuthenticationException("리프래시 토큰이 유효하지 않습니다.");
        }


        String newAccessToken = jwtUtil.createJWT(identifier, role, true);
        String newRefreshToken = jwtUtil.createJWT(identifier, role, false);

        saveRefreshToken(identifier, newRefreshToken);

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        response.addHeader("Set-Cookie", cookieComponent.createRefreshToken(newRefreshToken));

        System.out.println("success");

    }

    public void deleteRefreshTokenByIdentifier(String identifier) {
        UserEntity findUser = userService.findByIdentifier(identifier);
        refreshTokenRepository.deleteByUser(findUser);
    }

    public RefreshToken findByUserIdentifier(String identifier) {
        return refreshTokenRepository.findByUserIdentifier(identifier)
                .orElseThrow(() -> new AuthenticationException("리프래시 토큰이 존재하지 않습니다."));
    }
}
