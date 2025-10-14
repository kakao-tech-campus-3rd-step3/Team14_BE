package kakao.festapick.chat.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.dto.ChatRequestDto;
import kakao.festapick.chat.service.ChatMessageService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.redis.service.RedisPubSubService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final RedisPubSubService redisPubSubService;
    private final ChatRoomService chatRoomService;

    @MessageMapping("/{chatRoomId}/messages")
    public void sendChat(
            @DestinationVariable Long chatRoomId,
            @Valid @Payload ChatRequestDto requestDto,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getChatRoomByRoomId(chatRoomId);
        redisPubSubService.sendChatMessageToRedis(chatRoomResponseDto.roomId(), requestDto, userId);
    }
}
