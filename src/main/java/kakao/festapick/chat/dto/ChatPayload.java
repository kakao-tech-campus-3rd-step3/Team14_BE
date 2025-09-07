package kakao.festapick.chat.dto;

import java.time.LocalDateTime;

public record ChatPayload(
        Long festivalId,
        String senderName,
        String content,
        LocalDateTime sendDateTime
) {

}
