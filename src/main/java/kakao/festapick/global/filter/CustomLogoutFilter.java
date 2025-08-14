package kakao.festapick.global.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class CustomLogoutFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final JwtService jwtService;

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


        if(!jwtUtil.validateToken(refreshToken, false)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Claims claims = jwtUtil.getClaims(refreshToken);

        String identifier = claims.get("identifier").toString();

        jwtService.deleteRefreshTokenByIdentifier(identifier);

        response.addHeader("Set-Cookie", createCookie("refreshToken", null));
        response.setStatus(HttpServletResponse.SC_OK);

    }

    private String createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .path("/")
                .secure(false)
                .maxAge(0)
                .httpOnly(true)
                .build()
                .toString();
    }
}
