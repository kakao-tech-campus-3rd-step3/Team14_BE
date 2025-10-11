package kakao.festapick.wish.repository;

import java.util.List;
import java.util.Optional;

import kakao.festapick.wish.domain.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WishRepository extends JpaRepository<Wish, Long> {

    @Query(value = "select w from Wish w join fetch w.festival f where w.user.id = :userId",
            countQuery = "select count(w) from Wish w where w.user.id =:userId")
    Page<Wish> findByUserIdWithFestivalPage(Long userId, Pageable pageable);

    Optional<Wish> findByUserIdAndFestivalId(Long userId, Long festivalId);

    Optional<Wish> findByUserIdAndId(Long userId, Long wishId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Wish w where w.user.id = :userId")
    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Wish w where w.festival.id = :festivalId")
    void deleteByFestivalId(Long festivalId);

    @Query("select w from Wish w where w.festival.id = :festivalId")
    List<Wish> findByFestivalId(Long festivalId);

    @Query("select count(w) from Wish w where w.festival.id = :festivalId")
    long countByFestivalId(Long festivalId);
}
