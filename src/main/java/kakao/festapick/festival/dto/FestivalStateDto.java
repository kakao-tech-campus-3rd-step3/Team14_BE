package kakao.festapick.festival.dto;

import jakarta.validation.constraints.NotBlank;

public record FestivalStateDto(
        @NotBlank(message = "상태 반드시 선택해야합니다.")
        String state
) { }
