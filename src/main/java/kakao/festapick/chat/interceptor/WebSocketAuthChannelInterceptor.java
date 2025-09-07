package kakao.festapick.chat.interceptor;

import io.jsonwebtoken.Claims;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JWTUtil jwtUtil;
    private final OAuth2UserService oAuth2UserService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("-----------------------------------------------");
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {

            String authorization = Objects.requireNonNull(headerAccessor.getFirstNativeHeader("Authorization"));
            String accessToken = authorization.split(" ")[1];

            if(!jwtUtil.validateToken(accessToken, true)) {
                throw new AuthenticationException(ExceptionCode.INVALID_ACCESS_TOKEN);
            }
            Claims claims = jwtUtil.getClaims(accessToken);
            String identifier = claims.get("identifier").toString();

            UserEntity findUser = oAuth2UserService.findByIdentifier(identifier);
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+findUser.getRoleType().name()));
            Authentication auth = new UsernamePasswordAuthenticationToken(identifier, null, authorities);
            log.info(auth.toString());
            headerAccessor.setUser(auth);
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        ChannelInterceptor.super.postSend(message, channel, sent);
    }
}
