package kakao.festapick.review.dto;

import kakao.festapick.review.domain.Review;

public record ReviewResponseDto (
        Long reviewId,
        String reviewerName,
        String festivalTitle,
        String content,
        Integer score
) {

    public ReviewResponseDto(Review review) {
        this(review.getId(), review.getReviewerName(), review.getFestivalTitle(),
                review.getContent(), review.getScore());
    }
}
