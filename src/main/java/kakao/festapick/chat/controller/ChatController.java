package kakao.festapick.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.dto.PreviousMessagesResponseDto;
import kakao.festapick.chat.service.ChatMessageService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.global.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat API", description = "채팅 도메인 API")
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    @Operation(
            summary = "축제 아이디로 채팅방 아이디 조회",
            security = @SecurityRequirement(name = "JWT")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/festivals/{festivalId}/chatRooms")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> getChatRoomId(
            @PathVariable Long festivalId
    ) {
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getExistChatRoomOrMakeByFestivalId(
                festivalId);
        return new ResponseEntity<>(new ApiResponseDto<>(chatRoomResponseDto), HttpStatus.OK);
    }

    @Operation(
            summary = "채팅방의 이전 메세지 불러오기",
            security = @SecurityRequirement(name = "JWT")
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/chatRooms/{chatRoomId}/messages")
    public ResponseEntity<PreviousMessagesResponseDto> getPreviousMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "30", required = false) int size,
            @RequestParam(required = false) Long cursor
    ) {
        PreviousMessagesResponseDto payloads = chatMessageService.getPreviousMessages(chatRoomId, size, cursor);

        return new ResponseEntity<>(payloads, HttpStatus.OK);
    }

    @Operation(
            summary = "채팅방 목록 보기",
            security = @SecurityRequirement(name = "JWT")
    )
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
