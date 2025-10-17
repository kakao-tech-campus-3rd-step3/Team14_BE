package kakao.festapick.festival.service;

import kakao.festapick.festival.domain.Festival;
import kakao.festapick.review.domain.Review;
import kakao.festapick.wish.domain.Wish;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.OptionalDouble;

import static kakao.festapick.redis.util.RedisKeyNameConst.*;

@Component
@Transactional(readOnly = true)
public class FestivalCacheService {

    @Cacheable(value = FESTIVAL_IS_MY_WISH, key = "#userId + ':' + #festival.id", condition = "#userId != null")
    public boolean checkIsMyWish(Long userId, Festival festival) {
        if (userId == null) return false;

        for (Wish wish : festival.getWishes()) {
            if (userId.equals(wish.getUser().getId())) {
                return true;
            }
        }

        return false;
    }

    @Cacheable(value = FESTIVAL_REVIEW_SCORE, key = "#festival.id")
    public Double calculateReviewScore(Festival festival) {
        // 리뷰 별점 평균 계산
        OptionalDouble averageCalc = festival.getReviews()
                .stream().mapToDouble(Review::getScore).average();

        // 존재하는 리뷰가 없으면 null 반환
        return averageCalc.isPresent() ? averageCalc.getAsDouble() : null;
    }

    @Cacheable(value = FESTIVAL_WISH_COUNT, key = "#festival.id")
    public long getWishCount(Festival festival) {
        return festival.getWishes().size();
    }

}
