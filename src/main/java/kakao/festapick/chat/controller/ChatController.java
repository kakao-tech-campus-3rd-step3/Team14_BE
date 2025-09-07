package kakao.festapick.chat.controller;

import java.security.Principal;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/festival/{festivalId}")
    public void sendChat(
            @DestinationVariable Long festivalId,
            @RequestBody SendChatRequestDto requestDto,
            Principal principal) {
        String identifier = principal.getName();
        chatService.sendChat(festivalId, requestDto, identifier);
    }
}
