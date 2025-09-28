package kakao.festapick.wish.service;


import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.dto.WishResponseDto;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public WishResponseDto createWish(Long festivalId, Long userId) {
        Festival festival = festivalLowService.findFestivalById(festivalId);
        UserEntity user = userLowService.findById(userId);

        wishLowService.findByUserIdAndFestivalId(userId, festivalId)
                .ifPresent(w -> {
                    throw new DuplicateEntityException(ExceptionCode.WISH_DUPLICATE);
                });

        Wish newWish = new Wish(user, festival);
        Wish saved = wishLowService.save(newWish);
        FestivalDetailResponseDto festivalResponseDto = new FestivalDetailResponseDto(festival);
        return new WishResponseDto(saved.getId(), festivalResponseDto.id(), festivalResponseDto.title(),
                festivalResponseDto.areaCode());
    }

    public Page<WishResponseDto> getWishes(Long userId, Pageable pageable) {
        Page<Wish> wishes = wishLowService.findByUserIdWithFestivalPage(userId, pageable);
        return wishes.map(wish -> {
            FestivalDetailResponseDto responseDto = new FestivalDetailResponseDto(wish.getFestival());
            return new WishResponseDto(wish.getId() ,responseDto.id(), responseDto.title(),
                    responseDto.areaCode());
        });

    }

    @Transactional
    public void removeWish(Long wishId, Long userId) {
        Wish wish = wishLowService.findByUserIdAndId(userId, wishId);
        wishLowService.delete(wish);
    }


}
