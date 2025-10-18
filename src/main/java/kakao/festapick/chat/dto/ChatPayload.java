package kakao.festapick.chat.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import kakao.festapick.chat.domain.ChatMessage;

public record ChatPayload(
        Long id,
        Long userId,
        String senderName,
        String profileImgUrl,
        String content,
        String imageUrl,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime createdDate
) {

    public ChatPayload(ChatMessage chatMessage) {
        this(chatMessage.getId(), chatMessage.getUserId(), chatMessage.getSenderName(),
                chatMessage.getSenderProfileUrl(),
                chatMessage.getContent(), chatMessage.getImageUrl(), chatMessage.getCreatedDate());
    }
}
