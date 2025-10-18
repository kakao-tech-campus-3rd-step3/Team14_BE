package kakao.festapick.wish.service;


import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.service.FestivalCacheService;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.dto.WishResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishService {

    private final WishLowService wishLowService;
    private final UserLowService userLowService;
    private final FestivalLowService festivalLowService;

    private final FestivalCacheService festivalCacheService;

    // 좋아요 등록
    @Transactional
    public WishResponseDto createWish(Long festivalId, Long userId) {
        Festival festival = festivalLowService.findFestivalById(festivalId);
        UserEntity user = userLowService.getReferenceById(userId);

        // 이미 좋아요를 했으면 예외 반환
        if (wishLowService.existsByUserIdAndFestivalId(userId, festivalId))
                    throw new DuplicateEntityException(ExceptionCode.WISH_DUPLICATE);

        Wish newWish = new Wish(user, festival);
        Wish saved = wishLowService.save(newWish);

        return new WishResponseDto(saved.getId(), festival.getId(), userId,  festival.getTitle(), festival.getAreaCode());
    }

    // 유저의 좋아요 반환
    public Page<WishResponseDto> getWishes(Long userId, Pageable pageable) {
        Page<Wish> wishes = wishLowService.findByUserIdWithFestivalPage(userId, pageable);
        return wishes.map(wish -> {
            Festival festival = wish.getFestival();
            return new WishResponseDto(wish.getId() ,festival.getId(), userId, festival.getTitle(),
                    festival.getAreaCode());
        });

    }

    // 유저의 좋아요 반환
    public Page<FestivalListResponse> getWishedFestivals(Long userId, Pageable pageable) {
        Page<Wish> wishes = wishLowService.findByUserIdWithFestivalPage(userId, pageable);
        Page<Festival> festivalList = wishes.map(Wish::getFestival);
        return festivalList.map(festival -> {
            Double averageScore = festivalCacheService.calculateReviewScore(festival);
            long wishCount = festivalCacheService.getWishCount(festival);
            return new FestivalListResponse(festival, averageScore, wishCount);
        });
    }

    // wishId 기반 좋아요 삭제
    @Transactional
    public void removeWishWithWishId(Long wishId, Long userId) {
        Wish wish = wishLowService.findByUserIdAndId(userId, wishId);
        wishLowService.delete(wish);
    }

    // festivalId 기반 좋아요 삭제
    @Transactional
    public void removeWishWithFestivalId(Long festivalId, Long userId) {
        Wish wish = wishLowService.findByUserIdAndFestivalId(userId, festivalId);
        wishLowService.delete(wish);
    }


}
