package kakao.festapick.ai.controller;

import jakarta.validation.Valid;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.service.AiRecommendationService;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public ResponseEntity<Page<FestivalListResponse>> getRecommendedFestival(@AuthenticationPrincipal Long userId,
                                                                                             @RequestParam(defaultValue = "0") int page,
                                                                                             @RequestParam(defaultValue = "5") int size) {
        Page<FestivalListResponse> response = aiRecommendationService.getRecommendedFestivals(userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
