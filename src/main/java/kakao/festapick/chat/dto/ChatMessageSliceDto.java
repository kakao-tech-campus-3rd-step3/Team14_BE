package kakao.festapick.chat.dto;

import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;

public record ChatMessageSliceDto(
        List<ChatMessage> content,
        Boolean hasNext
) {

}
