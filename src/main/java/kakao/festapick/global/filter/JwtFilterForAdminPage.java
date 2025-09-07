package kakao.festapick.global.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.jwt.util.JwtUtil;
import kakao.festapick.jwt.util.TokenType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilterForAdminPage extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final OAuth2UserService oAuth2UserService;
    private final CookieComponent cookieComponent;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (request.getRequestURI().startsWith("/admin")) return false;
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie accessCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equalsIgnoreCase("accessToken"))
                .findFirst().orElse(null);

        if (accessCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = accessCookie.getValue();

        if (!jwtUtil.validateToken(accessToken, TokenType.ACCESS_TOKEN)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("유효하지 않은 토큰입니다.");
            return;
        }

        Claims claims = jwtUtil.getClaims(accessToken);
        String identifier = claims.get("identifier").toString();

        try {
            UserEntity findUser = oAuth2UserService.findByIdentifier(identifier);
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+findUser.getRoleType().name()));
            Authentication auth = new UsernamePasswordAuthenticationToken(identifier, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (NotFoundEntityException ex) {
            response.setHeader("Set-Cookie", cookieComponent.deleteAccessToken());
            response.sendRedirect("/login");
        }
    }
}
