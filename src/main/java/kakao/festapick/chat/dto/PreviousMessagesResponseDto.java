package kakao.festapick.chat.dto;

import java.util.List;

public record PreviousMessagesResponseDto(
        List<ChatPayload> content,
        Long cursor,
        Boolean hasMoreList
) {
    public PreviousMessagesResponseDto(List<ChatPayload> content, Boolean hasMoreList) {
        this(content, content.isEmpty() ? null : content.getFirst().id(), hasMoreList);
    }
}
