package kakao.festapick.chat.dto;

import java.time.LocalDateTime;

public record SendChatRequestDto(
        String content,
        LocalDateTime sendDateTime
) {

}
