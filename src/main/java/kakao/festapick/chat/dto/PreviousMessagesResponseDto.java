package kakao.festapick.chat.dto;

import java.util.List;

public record PreviousMessagesResponseDto(
        List<ChatPayload> content,
        Long nextBeforeId
) {
    public PreviousMessagesResponseDto(List<ChatPayload> content) {
        this(content, content.isEmpty() ? null : content.getFirst().id());
    }
}
