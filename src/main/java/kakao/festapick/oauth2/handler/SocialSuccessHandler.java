package kakao.festapick.oauth2.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.jwt.util.JwtUtil;
import kakao.festapick.jwt.service.JwtService;
import kakao.festapick.jwt.util.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtService jwtService;
    private final CookieComponent cookieComponent;
    private final String frontDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String identifier = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String requestURI = request.getRequestURI();

        if (requestURI.endsWith("ssr")) {
            String accessToken = jwtUtil.createJWT(identifier, "ROLE_" + role, TokenType.ACCESS_TOKEN);
            response.addHeader("Set-Cookie", cookieComponent.createAccessToken(accessToken));
            response.sendRedirect("/admin");
            return;
        }

        String refreshToken = jwtUtil.createJWT(identifier, "ROLE_"+role, TokenType.REFRESH_TOKEN);

        jwtService.saveRefreshToken(identifier, refreshToken);

        response.addHeader("Set-Cookie", cookieComponent.createRefreshToken(refreshToken));
        response.sendRedirect(frontDomain + "/cookie");
    }



}
