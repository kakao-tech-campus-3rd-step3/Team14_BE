package kakao.festapick.ai.controller;

import jakarta.validation.Valid;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.service.AiRecommendationService;
import kakao.festapick.dto.ApiResponseDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<List<FestivalListResponse>>> getAiRecommendations(@Valid @RequestBody AiRecommendationRequest aiRecommendationRequest) {

        List<FestivalListResponse> response = aiRecommendationService.getRecommendation(aiRecommendationRequest);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto(response));
    }
}
