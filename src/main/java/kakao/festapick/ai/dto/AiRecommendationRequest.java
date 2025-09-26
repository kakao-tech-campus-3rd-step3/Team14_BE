package kakao.festapick.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiRecommendationRequest(

        @NotNull
        Integer areaCode,
        @NotNull
        @Size(min = 1, max = 3, message = "선호하는 축제 스타일을 1개 이상 3개 이하 선택해주세요")
        List<FestivalStyle> styles,
        @NotNull(message = "응답하지 않은 설문이 존재합니다.")
        Boolean isNewPlace,
        @NotNull(message = "응답하지 않은 설문이 존재합니다.")
        Boolean isSolo,
        @NotNull(message = "응답하지 않은 설문이 존재합니다.")
        Boolean prefersEnjoyment,
        @NotNull(message = "응답하지 않은 설문이 존재합니다.")
        Boolean isSpontaneous,
        String additionalInfo
) {
}
