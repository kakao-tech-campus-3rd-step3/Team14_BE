package kakao.festapick.chat.dto;

import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;

public record ChatPayload(
        Long id,
        String senderName,
        String profileImgUrl,
        String content,
        String imageUrl
) {

    public ChatPayload(ChatMessage chatMessage, String imageUrl) {
        this(chatMessage.getId(), chatMessage.getSenderName(), chatMessage.getSenderProfileUrl(),
                chatMessage.getContent(), imageUrl);
    }
}
