package kakao.festapick.chat.dto;

import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;

public record ChatPayload(
        Long id,
        String senderName,
        String profileImgUrl,
        String content,
        List<String>imageUrls
) {

    public ChatPayload(ChatMessage chatMessage, List<String> imageUrls) {
        this(chatMessage.getId(), chatMessage.getSenderName(), chatMessage.getSenderProfileUrl(),
                chatMessage.getContent(), imageUrls);
    }
}
