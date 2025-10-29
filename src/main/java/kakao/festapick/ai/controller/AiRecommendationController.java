package kakao.festapick.ai.controller;

import jakarta.validation.Valid;
import kakao.festapick.ai.dto.AiRecommendationHistoryResponse;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.service.AiRecommendationService;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<List<FestivalListResponse>>> getAiRecommendations(@Valid @RequestBody AiRecommendationRequest aiRecommendationRequest,
                                                                                           @AuthenticationPrincipal Long userId) {

        List<FestivalListResponse> response = aiRecommendationService.getRecommendation(aiRecommendationRequest, userId);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto(response));
    }

    @GetMapping("/histories")
    public ResponseEntity<ApiResponseDto<AiRecommendationHistoryResponse>> getRecommendedFestival(@AuthenticationPrincipal Long userId) {
        AiRecommendationHistoryResponse response = aiRecommendationService.getRecommendedFestivals(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto(response));
    }

}
