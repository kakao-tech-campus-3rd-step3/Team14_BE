package kakao.festapick.ai.service;


import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final RestClient fastApiClient;
    private final RecommendationHistoryLowService recommendationHistoryLowService;
    private final UserLowService userLowService;
    private final FestivalLowService festivalLowService;

    public List<FestivalListResponse> getRecommendation(AiRecommendationRequest aiRecommendationRequest, Long userId) {


        UserEntity findUser = userLowService.findById(userId);

        ResponseEntity<List<FestivalListResponse>> response = fastApiClient.post()
                .uri("/festivals/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(aiRecommendationRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<FestivalListResponse>>(){});

        Set<Long> festivalIds = response.getBody()
                .stream().map(FestivalListResponse::id).collect(Collectors.toSet());

      recommendationHistoryLowService.findByFestivalsIdsAndUserId(festivalIds, userId)
              .forEach(recommendationHistory -> festivalIds.remove(recommendationHistory.getFestival().getId()));


        List<RecommendationHistory> recommendationHistories = festivalLowService.findAllById(festivalIds)
                .stream()
                .map(festival -> new RecommendationHistory(festival, findUser)).toList();

        recommendationHistoryLowService.saveAll(recommendationHistories);


        return response.getBody();
    }


    public Page<FestivalListResponse> getRecommendedFestivals(Long userId, Pageable pageable) {
        return recommendationHistoryLowService.findByUserIdWithFestival(userId, pageable)
                .map(recommendationHistory -> new FestivalListResponse(recommendationHistory.getFestival()));
    }
}
