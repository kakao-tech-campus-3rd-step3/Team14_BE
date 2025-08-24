package kakao.festapick.review.service;

import jakarta.validation.Valid;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OAuth2UserService oAuth2UserService;
    private final FestivalRepository festivalRepository;

    @Transactional
    public ReviewResponseDto createReview(Long festivalId, @Valid ReviewRequestDto requestDto, String identifier) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        UserEntity user = oAuth2UserService.findByIdentifier(identifier);

        if (reviewRepository.existsByUserIdAndFestivalId(user.getId(), festivalId)) {
            throw new DuplicateEntityException(ExceptionCode.REVIEW_DUPLICATE);
        }

        Review newReview = new Review(user, festival, requestDto.content(), requestDto.score());
        Review saved = reviewRepository.save(newReview);

        return new ReviewResponseDto(saved.getId(), user.getUsername(), festival.getTitle(),
                saved.getContent(), saved.getScore());
    }

    public Page<ReviewResponseDto> getFestivalReviews(Long festivalId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByFestivalId(festivalId, pageable);
        return reviews.map(review -> new ReviewResponseDto(review.getId(),
                review.getReviewerName(), review.getFestivalTitle(), review.getContent(),
                review.getScore()));
    }

    public Page<ReviewResponseDto> getMyReviews(String identifier, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserIdentifier(identifier, pageable);
        return reviews.map(review -> new ReviewResponseDto(review.getId(),
                review.getReviewerName(), review.getFestivalTitle(), review.getContent(),
                review.getScore()));
    }

    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, @Valid ReviewRequestDto requestDto, String identifier) {
        Review review = reviewRepository.findByUserIdentifierAndId(identifier, reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));

        review.changeContent(requestDto.content());
        review.changeScore(requestDto.score());

        return new ReviewResponseDto(review.getId(), review.getReviewerName(),
                review.getFestivalTitle(), review.getContent(), review.getScore());
    }

    @Transactional
    public void removeReview(Long reviewId, String identifier) {
        Review review = reviewRepository.findByUserIdentifierAndId(identifier, reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
    }
}
