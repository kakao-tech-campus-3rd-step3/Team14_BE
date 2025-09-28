package kakao.festapick.wish.service;

import java.util.List;
import java.util.Optional;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishLowService {

    private final WishRepository wishRepository;

    public Wish save(Wish wish){
        return wishRepository.save(wish);
    }

    public Page<Wish> findByUserIdWithFestivalPage(Long userId, Pageable pageable){
        return wishRepository.findByUserIdWithFestivalPage(userId, pageable);
    }

    public Optional<Wish> findByUserIdAndFestivalId(Long userId, Long festivalId){
        return wishRepository.findByUserIdAndFestivalId(userId, festivalId);
    }

    public Wish findByUserIdAndId(Long userId, Long wishId){
        return wishRepository.findByUserIdAndId(userId, wishId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.WISH_NOT_FOUND));
    }

    public void delete(Wish wish){
        wishRepository.delete(wish);
    }

    public void deleteByUserId(Long userId){
        wishRepository.deleteByUserId(userId);
    }

    public void deleteByFestivalId(Long festivalId){
        wishRepository.deleteByFestivalId(festivalId);
    }

    public List<Wish> findByFestivalId(Long festivalId){
        return wishRepository.findByFestivalId(festivalId);
    }

}
