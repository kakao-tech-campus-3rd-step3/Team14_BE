package kakao.festapick.global.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.jwt.service.JwtService;
import kakao.festapick.jwt.util.JwtUtil;
import kakao.festapick.jwt.util.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class CustomLogoutFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtService jwtService;
    private final CookieComponent cookieComponent;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (!HttpMethod.POST.name().equals(method) || !requestURI.equals("/api/users/logout")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        if(!jwtUtil.validateToken(refreshToken, TokenType.REFRESH_TOKEN)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Claims claims = jwtUtil.getClaims(refreshToken);

        String identifier = claims.get("identifier").toString();

        jwtService.deleteRefreshTokenByIdentifier(identifier);

        response.addHeader("Set-Cookie", cookieComponent.deleteRefreshToken());
        response.setStatus(HttpServletResponse.SC_OK);

    }
}
