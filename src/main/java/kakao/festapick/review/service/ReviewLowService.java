package kakao.festapick.review.service;

import java.util.List;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewLowService {

    private final ReviewRepository reviewRepository;


    @CacheEvict(value = "festival:reviewAvgScore", key = "#review.festival.id")
    public Review save(Review review){
        return reviewRepository.save(review);
    }

    public Review findById(Long reviewId){
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));
    }

    public boolean existsByUserIdAndFestivalId(Long userId, Long festivalId){
        return reviewRepository.existsByUserIdAndFestivalId(userId, festivalId);
    }

    public Page<Review> findByFestivalIdWithAll(Long festivalId, Pageable pageable){
        return reviewRepository.findByFestivalIdWithAll(festivalId, pageable);
    }

    public Page<Review> findByUserIdWithAll(Long userId, Pageable pageable){
        return reviewRepository.findByUserIdWithAll(userId, pageable);
    }

    public Review findByUserIdAndId(Long userId, Long reviewId){
        return reviewRepository.findByUserIdAndId(userId, reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));
    }

    @CacheEvict(value = "festival:reviewAvgScore", key = "#review.festival.id")
    public void delete(Review review){
        reviewRepository.delete(review);
    }

    @CacheEvict(value = "festival:reviewAvgScore", allEntries = true)
    public void deleteByUserId(Long userId){
        reviewRepository.deleteByUserId(userId);
    }

    @CacheEvict(value = "festival:reviewAvgScore", allEntries = true)
    public void deleteByFestivalId(Long festivalId){
        reviewRepository.deleteByFestivalId(festivalId);
    }

    public List<Review> findByFestivalId(Long festivalId){
        return reviewRepository.findByFestivalId(festivalId);
    }

    public List<Review> findByUserId(Long userId){
        return reviewRepository.findByUserId(userId);
    }

}
