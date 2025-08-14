package kakao.festapick.oauth2.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.domain.RefreshToken;
import kakao.festapick.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final JwtService jwtService;
    private final CookieComponent cookieComponent;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String identifier = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String state = request.getParameter("state");

        String refreshToken = jwtUtil.createJWT(identifier, "ROLE_"+role, false);

        jwtService.saveRefreshToken(identifier, refreshToken);

        response.addHeader("Set-Cookie", cookieComponent.createRefreshToken(refreshToken));
        response.sendRedirect("http://localhost:3000/cookie?redirect="+state);
    }



}
