package kakao.festapick.config;

import java.util.Optional;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomSessionStatusDto;
import kakao.festapick.chat.interceptor.WebSocketAuthChannelInterceptor;
import kakao.festapick.chat.service.ChatParticipantLowService;
import kakao.festapick.chat.service.ChatRoomSessionLowService;
import kakao.festapick.global.StompInterceptorExceptionHandler;
import kakao.festapick.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatRoomSessionLowService chatRoomSessionLowService;
    private final ChatParticipantLowService chatParticipantLowService;
    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;
    private final StompInterceptorExceptionHandler stompInterceptorExceptionHandler;
    @Value("${spring.front.domain}")
    private String frontDomain;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "/queue")
                .setHeartbeatValue(new long[]{15_000, 15_000})
                .setTaskScheduler(heartBeatScheduler());
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp").setAllowedOriginPatterns(frontDomain);

        registry.addEndpoint("/stomp")
                .setAllowedOriginPatterns(frontDomain)
                .withSockJS();

        registry.setErrorHandler(stompInterceptorExceptionHandler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }

    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("web-socket-heartbeat-");

        return taskScheduler;
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Optional<ChatRoomSessionStatusDto> optionalChatRoomSessionStatusDto = chatRoomSessionLowService.deleteBySessionId(
                sessionId);
        if (optionalChatRoomSessionStatusDto.isPresent()) {
            ChatRoomSessionStatusDto chatRoomSessionStatusDto = optionalChatRoomSessionStatusDto.get();
            ChatParticipant participant = chatParticipantLowService.findByChatRoomIdAndUserIdWithChatRoom(
                    chatRoomSessionStatusDto.chatRoomId(), chatRoomSessionStatusDto.userId());

            UserEntity user = participant.getUser();
            ChatRoom chatRoom = participant.getChatRoom();

            chatParticipantLowService.syncMessageSeq(user.getId(), chatRoom.getId(), chatRoom.getMessageSeq());
        }

    }
}