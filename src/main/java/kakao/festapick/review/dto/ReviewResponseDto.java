package kakao.festapick.review.dto;

public record ReviewResponseDto (
        Long reviewId,
        String reviewerName,
        String festivalTitle,
        String content,
        Integer score
) {

}
