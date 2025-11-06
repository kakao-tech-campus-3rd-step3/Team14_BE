package kakao.festapick.ai.service;


import kakao.festapick.ai.domain.RecommendationForm;
import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.dto.AiRecommendationHistoryResponse;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.dto.AiRecommendationResponse;
import kakao.festapick.ai.dto.RecommendationFormResponse;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.service.FestivalCacheService;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final RestClient fastApiClient;
    private final RecommendationHistoryLowService recommendationHistoryLowService;
    private final UserLowService userLowService;
    private final FestivalLowService festivalLowService;
    private final FestivalCacheService festivalCacheService;
    private final RecommendationFormLowService recommendationFormLowService;

    public List<FestivalListResponse> getRecommendation(AiRecommendationRequest aiRecommendationRequest, Long userId) {

        ResponseEntity<List<AiRecommendationResponse>> response = fastApiClient.post()
                .uri("/ai/recommend/model")
                .contentType(MediaType.APPLICATION_JSON)
                .body(aiRecommendationRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<AiRecommendationResponse>>(){});


        List<AiRecommendationResponse> aiRecommendationResponses1 = response.getBody();

        for (AiRecommendationResponse aiRecommendationResponse : aiRecommendationResponses1) {
            System.out.println(aiRecommendationResponse.toString());
        }

        // 최신 추천 기록만 남기고 저장
        recommendationHistoryLowService.deleteByUserId(userId);

        UserEntity findUser = userLowService.getReferenceById(userId);

        List<AiRecommendationResponse> aiRecommendationResponses = response.getBody();

        List<RecommendationHistory> recommendationHistories = aiRecommendationResponses
                .stream().map(festivalInfo ->
                        new RecommendationHistory(festivalLowService.getReferenceById(festivalInfo.id()), findUser))
                .toList();

        recommendationHistoryLowService.saveAll(recommendationHistories);

        // 최신 추천 설문 기록만 남기고 저장
        recommendationFormLowService.deleteByUserId(userId);
        recommendationFormLowService.save(new RecommendationForm(aiRecommendationRequest, findUser));

        List<FestivalListResponse> festivalListResponses = aiRecommendationResponses
                .stream()
                .map(festivalInfo -> {
                            Festival festival = festivalLowService.getReferenceById(festivalInfo.id());
                            long wishCount = festivalCacheService.getWishCount(festival);
                            Double reviewScore = festivalCacheService.calculateReviewScore(festival);
                            return new FestivalListResponse(festivalInfo, reviewScore, wishCount);
                        }
                ).toList();

        return festivalListResponses;
    }


    public AiRecommendationHistoryResponse getRecommendedFestivals(Long userId) {
        List<FestivalListResponse> festivalListResponses = recommendationHistoryLowService.findByUserIdWithFestival(userId)
                .stream().map(recommendationHistory -> {
                    Festival festival = recommendationHistory.getFestival();
                    Double averageScore = festivalCacheService.calculateReviewScore(festival);
                    long wishCount = festivalCacheService.getWishCount(festival);
                    return new FestivalListResponse(festival, averageScore, wishCount);
                }).toList();

        if (festivalListResponses.isEmpty())
            return new AiRecommendationHistoryResponse(festivalListResponses, null);

        RecommendationFormResponse recommendationFormResponse = new RecommendationFormResponse(recommendationFormLowService.findByUserId(userId));

        return new AiRecommendationHistoryResponse(festivalListResponses, recommendationFormResponse);
    }

}
