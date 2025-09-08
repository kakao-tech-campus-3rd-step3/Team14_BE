package kakao.festapick.wish.service;


import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserService;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishService {

    private final WishRepository wishRepository;
    private final UserService userService;
    private final FestivalRepository festivalRepository;

    @Transactional
    public WishResponseDto createWish(Long festivalId, String identifier) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        UserEntity user = userService.findByIdentifier(identifier);

        wishRepository.findByUserIdentifierAndFestivalId(identifier, festivalId)
                .ifPresent(w -> {
                    throw new DuplicateEntityException(ExceptionCode.WISH_DUPLICATE);
                });

        Wish newWish = new Wish(user, festival);
        Wish saved = wishRepository.save(newWish);
        FestivalDetailResponseDto festivalResponseDto = new FestivalDetailResponseDto(festival);
        return new WishResponseDto(saved.getId(), festivalResponseDto.id(), festivalResponseDto.title(),
                festivalResponseDto.areaCode());
    }

    public Page<WishResponseDto> getWishes(String identifier, Pageable pageable) {
        Page<Wish> wishes = wishRepository.findByUserIdentifier(identifier, pageable);
        return wishes.map(wish -> {
            FestivalDetailResponseDto responseDto = new FestivalDetailResponseDto(wish.getFestival());
            return new WishResponseDto(wish.getId() ,responseDto.id(), responseDto.title(),
                    responseDto.areaCode());
        });

    }

    @Transactional
    public void removeWish(Long wishId, String identifier) {
        Wish wish = wishRepository.findByUserIdentifierAndId(identifier, wishId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.WISH_NOT_FOUND));

        wishRepository.delete(wish);
    }


}
