package kakao.festapick.wish.service;

import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalResponseDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishService {

    private final WishRepository wishRepository;
    private final OAuth2UserService oAuth2UserService;
    private final FestivalRepository festivalRepository;

    @Transactional
    public WishResponseDto createWish(Long festivalId, String identifier) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 축제입니다."));
        UserEntity user = oAuth2UserService.findByIdentifier(identifier);

        wishRepository.findByUserIdentifierAndFestivalId(identifier, festivalId)
                .ifPresent(w -> {
                    throw new DuplicateEntityException("이미 좋아요한 축제입니다.");
                });

        Wish newWish = new Wish(user, festival);
        wishRepository.save(newWish);
        FestivalResponseDto festivalResponseDto = new FestivalResponseDto(festival);
        return new WishResponseDto(festivalResponseDto);
    }

    //todo 페이지네이션
    public List<WishResponseDto> getWishes(String identifier) {
        List<Wish> wishes = wishRepository.findByUserIdentifier(identifier);
        List<WishResponseDto> responseDtoList = new ArrayList<>();
        for (Wish wish : wishes) {
            FestivalResponseDto festivalResponseDto = new FestivalResponseDto(wish.getFestival());
            WishResponseDto responseDto = new WishResponseDto(festivalResponseDto);
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    @Transactional
    public void removeWish(Long festivalId, String identifier) {
        Wish wish = wishRepository.findByUserIdentifierAndFestivalId(identifier, festivalId)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 좋아요입니다."));

        wishRepository.delete(wish);
    }


}
