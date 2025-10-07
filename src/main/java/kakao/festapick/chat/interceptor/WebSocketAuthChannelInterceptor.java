package kakao.festapick.chat.interceptor;

import io.jsonwebtoken.Claims;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.service.ChatParticipantService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.WebSocketException;
import kakao.festapick.jwt.util.JwtUtil;
import kakao.festapick.jwt.util.TokenType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
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
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CHATROOM_DEST_PATTERN = Pattern.compile("^/sub/(\\d+)/messages$");
    private static final String USER_ERROR_DEST = "/user/queue/errors";

    private final JwtUtil jwtUtil;
    private final UserLowService userLowService;
    private final ChatParticipantService chatParticipantService;
    private final ChatRoomService chatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            String authorization = headerAccessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AuthenticationException(ExceptionCode.ACCESS_TOKEN_NOT_EXIST);
            }

            String accessToken = authorization.split(" ")[1];

            if (!jwtUtil.validateToken(accessToken, TokenType.ACCESS_TOKEN)) {
                throw new AuthenticationException(ExceptionCode.INVALID_ACCESS_TOKEN);
            }

            Claims claims = jwtUtil.getClaims(accessToken);
            String identifier = claims.get("identifier").toString();

            UserEntity findUser = userLowService.findByIdentifier(identifier);
            Long userId = findUser.getId();
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + findUser.getRoleType().name()));
            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null,
                    authorities);
            headerAccessor.setUser(auth);
        } else if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            Principal principal = headerAccessor.getUser();
            String destination = headerAccessor.getDestination();
            if (principal == null) {
                throw new AuthenticationException(ExceptionCode.NO_LOGIN);
            }
            if (destination == null) {
                throw new WebSocketException(ExceptionCode.MISSING_DESTINATION);
            }
            checkDestination(destination, principal);
        }

        return message;
    }

    private void checkDestination(String destination, Principal principal) {
        Matcher matcher = CHATROOM_DEST_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long userId = Long.valueOf(principal.getName());
            Long chatRoomId = Long.valueOf(matcher.group(1));
            ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getChatRoomByRoomId(chatRoomId);
            chatParticipantService.enterChatRoom(userId, chatRoomResponseDto.roomId());
        } else if (!destination.equals(USER_ERROR_DEST)) {
            throw new WebSocketException(ExceptionCode.INVALID_DESTINATION);
        }
    }
}
