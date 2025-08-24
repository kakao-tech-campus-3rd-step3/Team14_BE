package kakao.festapick.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequestDto(
        @NotBlank(message = "내용을 입력해 주세요")
        String content,
        @NotNull
        @Min(value = 1, message = "점수는 최소 1점에서 최대 5점 까지 가능합니다")
        @Max(value = 5, message = "점수는 최소 1점에서 최대 5점 까지 가능합니다")
        Integer score
) {

}
