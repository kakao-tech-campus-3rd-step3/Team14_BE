package kakao.festapick.review.dto;

import kakao.festapick.review.domain.Review;

import java.util.ArrayList;
import java.util.List;

public record ReviewResponseDto (
        Long reviewId,
        String reviewerName,
        String festivalTitle,
        String content,
        Integer score,
        List<String> imageUrls,
        String videoUrl

) {

    public ReviewResponseDto(Review review, List<String> imageUrls, String videoUrl) {
        this(review.getId(), review.getReviewerName(), review.getFestivalTitle(),
                review.getContent(), review.getScore(), imageUrls, videoUrl);
    }
}
