package kakao.festapick.ai.service;


import kakao.festapick.ai.domain.RecommendationForm;
import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.service.FestivalCacheService;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        ResponseEntity<List<FestivalListResponse>> response = fastApiClient.post()
                .uri("/festivals/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(aiRecommendationRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<FestivalListResponse>>(){});

        // 최신 추천 기록만 남기고 저장
        recommendationHistoryLowService.deleteByUserId(userId);

        UserEntity findUser = userLowService.getReferenceById(userId);

        List<RecommendationHistory> recommendationHistories = response.getBody()
                .stream().map(festivalListResponse ->
                        new RecommendationHistory(festivalLowService.getReferenceById(festivalListResponse.id()), findUser))
                .toList();

        recommendationHistoryLowService.saveAll(recommendationHistories);

        // 최신 추천 설문 기록만 남기고 저장
        recommendationFormLowService.deleteByUserId(userId);
        recommendationFormLowService.save(new RecommendationForm(aiRecommendationRequest, findUser));

        return response.getBody();
    }


    public Page<FestivalListResponse> getRecommendedFestivals(Long userId, Pageable pageable) {
        return recommendationHistoryLowService.findByUserIdWithFestival(userId, pageable)
                .map(recommendationHistory -> {
                    Festival festival = recommendationHistory.getFestival();
                    Double averageScore = festivalCacheService.calculateReviewScore(festival);
                    long wishCount = festivalCacheService.getWishCount(festival);
                    return new FestivalListResponse(festival, averageScore, wishCount);
                });
    }

}
