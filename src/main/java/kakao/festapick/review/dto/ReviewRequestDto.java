package kakao.festapick.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewRequestDto(
        @NotBlank(message = "내용을 입력해 주세요")
        @Size(min = 10, max= 500, message = "리뷰는 10자 이상, 500자 이내로 작성할 수 있습니다")
        String content,
        @NotNull
        @Min(value = 1, message = "점수는 최소 1점에서 최대 5점 까지 가능합니다")
        @Max(value = 5, message = "점수는 최소 1점에서 최대 5점 까지 가능합니다")
        Integer score,
        List<String> imageUrls,
        String videoUrl // 일단 video url은 한개만 제한
) {

}
