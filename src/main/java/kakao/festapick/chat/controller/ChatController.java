package kakao.festapick.chat.controller;

import java.security.Principal;
import java.util.List;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.chat.service.ChatMessageService;
import kakao.festapick.chat.service.ChatParticipantService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final ChatParticipantService chatParticipantService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/festivals/{festivalId}/chatRooms")
    public ResponseEntity<ApiResponseDto<Long>> getChatRoomId(
            @PathVariable Long festivalId
    ) {
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getExistChatRoomOrMakeByFestivalId(
                festivalId);
        return new ResponseEntity<>(new ApiResponseDto<>(chatRoomResponseDto.roomId()),
                HttpStatus.OK);
    }

    @MessageMapping("/{chatRoomId}/messages")
    public void sendChat(
            @DestinationVariable Long chatRoomId,
            @Payload SendChatRequestDto requestDto,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getChatRoomByRoomId(chatRoomId);
        chatMessageService.sendChat(chatRoomResponseDto.roomId(), requestDto, userId);
    }

    @SubscribeMapping("/{chatRoomId}/messages")
    public void enterChatRoom(
            @DestinationVariable Long chatRoomId,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getChatRoomByRoomId(chatRoomId);
        chatParticipantService.enterChatRoom(userId, chatRoomResponseDto.roomId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/chatRooms/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatPayload>> getPreviousMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "30", required = false) int size,
            @RequestParam(defaultValue = "0", required = false) int page
    ) {
        Page<ChatPayload> payloads = chatMessageService.getPreviousMessages(chatRoomId,
                PageRequest.of(page, size));

        return new ResponseEntity<>(payloads, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/chatRooms")
    public ResponseEntity<Page<ChatRoomResponseDto>> getChatRooms(
            @RequestParam(defaultValue = "15", required = false) int size,
            @RequestParam(defaultValue = "0", required = false) int page
    ) {
        Page<ChatRoomResponseDto> chatRooms = chatRoomService.getChatRooms(
                PageRequest.of(page, size));

        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

}
