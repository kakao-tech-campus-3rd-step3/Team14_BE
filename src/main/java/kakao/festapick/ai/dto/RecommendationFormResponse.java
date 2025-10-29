package kakao.festapick.ai.dto;

import kakao.festapick.ai.domain.RecommendationForm;

import java.util.List;

public record RecommendationFormResponse(
        Integer areaCode,
        List<FestivalStyle> styles,
        Boolean isNewPlace,
        Boolean isSolo,
        Boolean prefersEnjoyment,
        Boolean isSpontaneous,
        String additionalInfo,
        Long userId
) {

    public RecommendationFormResponse(RecommendationForm recommendationForm) {
        this(
                recommendationForm.getAreaCode(),recommendationForm.getStyles(),
                recommendationForm.isNewPlace(), recommendationForm.isSolo(),
                recommendationForm.isPrefersEnjoyment(), recommendationForm.isSpontaneous(),
                recommendationForm.getAdditionalInfo(), recommendationForm.getUser().getId()
                );
    }

}
