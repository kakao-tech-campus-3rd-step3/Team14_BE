package kakao.festapick.wish.service;

import java.util.List;
import java.util.Optional;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.redis.util.RedisKeyNameConst;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static kakao.festapick.redis.util.RedisKeyNameConst.*;

@Service
@RequiredArgsConstructor
public class WishLowService {

    private final WishRepository wishRepository;

    @Caching(
            evict = {
                    @CacheEvict(value = FESTIVAL_IS_MY_WISH, key = "#wish.user.id + ':' + #wish.festival.id"),
                    @CacheEvict(value = FESTIVAL_WISH_COUNT, key = "#wish.festival.id")
            }
    )
    public Wish save(Wish wish){
        return wishRepository.save(wish);
    }

    public Page<Wish> findByUserIdWithFestivalPage(Long userId, Pageable pageable){
        return wishRepository.findByUserIdWithFestivalPage(userId, pageable);
    }

    public Wish findByUserIdAndFestivalId(Long userId, Long festivalId){
        return wishRepository.findByUserIdAndFestivalId(userId, festivalId)
                .orElseThrow(()-> new NotFoundEntityException(ExceptionCode.WISH_NOT_FOUND));
    }

    public boolean existsByUserIdAndFestivalId(Long userId, Long festivalId){
        Optional<Wish> findWish = wishRepository.findByUserIdAndFestivalId(userId, festivalId);
        if (findWish.isPresent()) return true;
        else return false;
    }

    public Wish findByUserIdAndId(Long userId, Long wishId){
        return wishRepository.findByUserIdAndId(userId, wishId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.WISH_NOT_FOUND));
    }

    @Caching(
            evict = {
                    @CacheEvict(value = FESTIVAL_IS_MY_WISH, key = "#wish.user.id + ':' + #wish.festival.id"),
                    @CacheEvict(value = FESTIVAL_WISH_COUNT, key = "#wish.festival.id")
            }
    )
    public void delete(Wish wish){
        wishRepository.delete(wish);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = FESTIVAL_IS_MY_WISH, allEntries = true),
                    @CacheEvict(value = FESTIVAL_WISH_COUNT, allEntries = true)
            }
    )
    public void deleteByUserId(Long userId){
        wishRepository.deleteByUserId(userId);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = FESTIVAL_IS_MY_WISH, allEntries = true),
                    @CacheEvict(value = FESTIVAL_WISH_COUNT, allEntries = true)
            }
    )
    public void deleteByFestivalId(Long festivalId){
        wishRepository.deleteByFestivalId(festivalId);
    }

    public List<Wish> findByFestivalId(Long festivalId){
        return wishRepository.findByFestivalId(festivalId);
    }

}
