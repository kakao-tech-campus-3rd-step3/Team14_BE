package kakao.festapick.chat.dto;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;

public record PreviousMessagesResponseDto(
        List<ChatPayload> content,
        Long cursorId,
        LocalDateTime cursorTime,
        Boolean hasMoreList
) {

    public PreviousMessagesResponseDto(List<ChatMessage> content, Boolean hasMoreList) {
        this(content.stream().map(ChatPayload::new).toList(),
                content.isEmpty() ? null : content.getFirst().getId(),
                content.isEmpty() ? null : content.getFirst().getCreatedDate(), hasMoreList);
    }
}
