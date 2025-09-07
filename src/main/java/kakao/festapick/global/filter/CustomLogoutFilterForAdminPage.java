package kakao.festapick.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class CustomLogoutFilterForAdminPage extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieComponent cookieComponent;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (!HttpMethod.POST.name().equals(method) || !requestURI.equals("/admin/logout")) {
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

        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("accessToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (accessToken == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        if(!jwtUtil.validateToken(accessToken, true)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.addHeader("Set-Cookie", cookieComponent.deleteAccessToken());
        response.sendRedirect("/login");
        response.setStatus(HttpServletResponse.SC_OK);

    }
}
