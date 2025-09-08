package kakao.festapick.review.controller;

import jakarta.validation.Valid;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()") // 리뷰 작성은 인증 필요
    @PostMapping("/festivals/{festivalId}/reviews")
    public ResponseEntity<Void> createReview(
            @AuthenticationPrincipal String identifier,
            @PathVariable Long festivalId,
            @Valid @RequestBody ReviewRequestDto requestDto) {

        Long reviewId = reviewService.createReview(festivalId, requestDto, identifier);

        return ResponseEntity.created(URI.create("/api/reviews/" + reviewId)).build();
    }


    @GetMapping("/festivals/{festivalId}/reviews") // 리뷰 조회는 인증 불필요
    public ResponseEntity<Page<ReviewResponseDto>> getFestivalReviews(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,
            @PathVariable Long festivalId
    ) {
        return new ResponseEntity<>(reviewService.getFestivalReviews(festivalId, pageable),
                HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()") // 내 리뷰만 조회는 인증 필요
    @GetMapping("/reviews/my")
    public ResponseEntity<Page<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal String identifier,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new ResponseEntity<>(reviewService.getMyReviews(identifier, pageable),
                HttpStatus.OK);
    }

    @GetMapping("/reviews/{reviewId}") // 리뷰 PK를 통한 것은 인증 불필요
    public ResponseEntity<ReviewResponseDto> getReview( @PathVariable Long reviewId) {

        ReviewResponseDto response = reviewService.getReview(reviewId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PreAuthorize("isAuthenticated()") // 리뷰 수정은 인증 필요
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String identifier,
            @Valid @RequestBody ReviewRequestDto requestDto) {

        reviewService.updateReview(reviewId, requestDto, identifier);


        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("isAuthenticated()") // 리뷰 삭제는 인증 필요
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> removeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String identifier) {
        reviewService.removeReview(reviewId, identifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
