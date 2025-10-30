package kakao.festapick.ai.dto;

import kakao.festapick.festival.dto.FestivalListResponse;

import java.util.List;

public record AiRecommendationHistoryResponse(
        List<FestivalListResponse> recommendedFestivals,
        RecommendationFormResponse recommendationFormResponse
) {

}
