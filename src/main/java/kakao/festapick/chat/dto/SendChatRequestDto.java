package kakao.festapick.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record SendChatRequestDto(
        @NotBlank
        String content
) {

}
