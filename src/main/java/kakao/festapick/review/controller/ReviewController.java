package kakao.festapick.review.controller;

import jakarta.validation.Valid;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.service.ReviewService;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/festivals/{festivalId}/reviews")
    public ResponseEntity<ReviewResponseDto> createReview(
            @AuthenticationPrincipal String identifier,
            @PathVariable Long festivalId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        return new ResponseEntity<>(reviewService.createReview(festivalId, requestDto, identifier),
                HttpStatus.CREATED);
    }

    @GetMapping("/festivals/{festivalId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getFestivalReviews(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,
            @PathVariable Long festivalId
    ) {
        return new ResponseEntity<>(reviewService.getFestivalReviews(festivalId, pageable),
                HttpStatus.OK);
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal String identifier,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new ResponseEntity<>(reviewService.getMyReviews(identifier, pageable),
                HttpStatus.OK);
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String identifier,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        return new ResponseEntity<>(reviewService.updateReview(reviewId, requestDto, identifier),
                HttpStatus.OK);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> removeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String identifier) {
        reviewService.removeReview(reviewId, identifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
