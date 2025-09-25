package kakao.festapick.ai.controller;

import jakarta.validation.Valid;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.service.AiRecommendationService;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping
    public ResponseEntity<List<FestivalListResponse>> getAiRecommendations(@Valid @RequestBody AiRecommendationRequest aiRecommendationRequest) {

        List<FestivalListResponse> response = aiRecommendationService.getRecommendation(aiRecommendationRequest);

        return ResponseEntity.ok(response);
    }
}
