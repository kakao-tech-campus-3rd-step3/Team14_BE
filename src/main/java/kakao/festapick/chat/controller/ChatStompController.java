package kakao.festapick.chat.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.chat.service.ChatMessageService;
import kakao.festapick.chat.service.ChatParticipantService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    @MessageMapping("/{chatRoomId}/messages")
    public void sendChat(
            @DestinationVariable Long chatRoomId,
            @Valid @Payload SendChatRequestDto requestDto,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getChatRoomByRoomId(chatRoomId);
        chatMessageService.sendChat(chatRoomResponseDto.roomId(), requestDto, userId);
    }
}
