package kakao.festapick.chat.interceptor;

import io.jsonwebtoken.Claims;
import java.security.Principal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.dto.ReadEventPayload;
import kakao.festapick.chat.service.ChatParticipantLowService;
import kakao.festapick.chat.service.ChatParticipantService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.chat.service.ChatRoomSessionLowService;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.WebSocketException;
import kakao.festapick.jwt.util.JwtUtil;
import kakao.festapick.jwt.util.TokenType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CHATROOM_DEST_PATTERN = Pattern.compile("^/sub/(\\d+)/messages$");
    private static final Pattern PUB_DEST_PATTERN = Pattern.compile(
            "^/pub/(\\d+)/(messages)$");
    private static final String USER_ERROR_DEST = "/user/queue/errors";
    private static final String USER_UNREADS_DEST = "/user/queue/unreads";
    private static final String USER_READS_DEST = "/user/queue/reads";

    private final JwtUtil jwtUtil;
    private final UserLowService userLowService;
    private final ChatParticipantService chatParticipantService;
    private final ChatRoomService chatRoomService;
    private final ChatParticipantLowService chatParticipantLowService;
    private final ChatRoomSessionLowService chatRoomSessionLowService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        StompCommand command = headerAccessor.getCommand();
        if (command == null) {
            return message;
        }

        switch (command) {
            case StompCommand.CONNECT -> handleConnect(headerAccessor);
            case StompCommand.SUBSCRIBE -> handleSubscribe(headerAccessor);
            case StompCommand.SEND -> handleSend(headerAccessor);
            case StompCommand.UNSUBSCRIBE -> handleUnSubscribeAndDisconnect(headerAccessor);
            default -> {
                // 그 외 command는 무시
            }
        }
        return message;
    }

    // 들어온 command가 Send인 경우
    private void handleSend(StompHeaderAccessor headerAccessor) {
        Optional.ofNullable(headerAccessor.getUser())
                .orElseThrow(() -> new AuthenticationException(ExceptionCode.NO_LOGIN));
        String destination = Optional.ofNullable(headerAccessor.getDestination())
                .orElseThrow(() -> new WebSocketException(ExceptionCode.MISSING_DESTINATION));

        //destination 확인
        checkSendDestination(destination);
    }

    // 들어온 command가 Subscribe인 경우
    private void handleSubscribe(StompHeaderAccessor headerAccessor) {
        //destination 확인
        checkDestination(headerAccessor);
    }

    // 들어온 command가 unSubscribe인 경우
    private void handleUnSubscribeAndDisconnect(StompHeaderAccessor headerAccessor) {
        //destination 확인
        checkDestination(headerAccessor);
    }

    // 들어온 command가 Connect인 경우
    private void handleConnect(StompHeaderAccessor headerAccessor) {
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
    }

    // 클라이언트에서 보낸 pub 메시지의 destination 검증
    private void checkSendDestination(String destination) {
        Matcher matcher = PUB_DEST_PATTERN.matcher(destination);

        // 채팅방 destination이 아니면 전부 예외처리
        if (!matcher.matches()) {
            throw new WebSocketException(ExceptionCode.INVALID_DESTINATION);
        }
    }

    // 클라이언트에서 보낸 sub 메시지의 destination 검증
    private void checkDestination(StompHeaderAccessor headerAccessor) {

        Principal principal = Optional.ofNullable(headerAccessor.getUser())
                .orElseThrow(() -> new AuthenticationException(ExceptionCode.NO_LOGIN));
        String destination = Optional.ofNullable(headerAccessor.getDestination())
                .orElseThrow(() -> new WebSocketException(ExceptionCode.MISSING_DESTINATION));

        Matcher matcher = CHATROOM_DEST_PATTERN.matcher(destination);

        StompCommand command = headerAccessor.getCommand();

        // 채팅방 구독이거나
        if (matcher.matches() && command == StompCommand.SUBSCRIBE) {
            Long userId = Long.valueOf(principal.getName());
            Long chatRoomId = Long.valueOf(matcher.group(1));
            String sessionId = headerAccessor.getSessionId();
            ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getChatRoomByRoomId(
                    chatRoomId);
            // 채팅방에 들어간 내 세션 등록
            chatRoomSessionLowService.increaseChatRoomSession(chatRoomId, userId, sessionId);
            // 채팅방 진입 시 읽음 처리
            ChatParticipant participant = chatParticipantService.enterChatRoom(userId, chatRoomResponseDto.roomId());
            ChatRoom chatRoom = participant.getChatRoom();
            chatParticipantLowService.syncMessageSeq(userId, chatRoomId, chatRoom.getMessageSeq());
            return;
        }

        // 채팅방 나갔을 때(UNSUBSCRIBE) 레디스 ChatRoomSession 무효화
        if (matcher.matches() && (command == StompCommand.UNSUBSCRIBE)) {
            Long userId = Long.valueOf(principal.getName());
            Long chatRoomId = Long.valueOf(matcher.group(1));
            String sessionId = headerAccessor.getSessionId();
            // 채팅방에 등록한 내 세션 삭제
            chatRoomSessionLowService.decreaseChatRoomSession(chatRoomId, userId, sessionId);
            // 채팅방 퇴장 시 읽음 처리
            ChatParticipant participant = chatParticipantLowService.findByChatRoomIdAndUserIdWithChatRoom(chatRoomId, userId);
            ChatRoom chatRoom = participant.getChatRoom();
            chatParticipantLowService.syncMessageSeq(userId, chatRoomId, chatRoom.getMessageSeq());
            return;
        }

        // 개인 채널 구독이 아니면 예외 발생
        if (!(destination.equals(USER_ERROR_DEST) || destination.equals(USER_UNREADS_DEST) || destination.equals(USER_READS_DEST))) {
            throw new WebSocketException(ExceptionCode.INVALID_DESTINATION); // 셋 다 아니면 예외 발생
        }
    }

}
