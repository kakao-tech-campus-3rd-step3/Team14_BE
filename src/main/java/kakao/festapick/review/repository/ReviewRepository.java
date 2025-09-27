package kakao.festapick.review.repository;

import kakao.festapick.review.domain.Review;
import kakao.festapick.user.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "select (count(r) > 0) from Review r where r.user.id= :userId and r.festival.id= :festivalId")
    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    @Query(value = "select r from Review r join fetch r.festival f join fetch r.user u where f.id= :festivalId",
            countQuery = "select count(r) from Review r where r.festival.id= :festivalId")
    Page<Review> findByFestivalIdWithAll(Long festivalId, Pageable pageable);

    @Query(value = "select r from Review r join fetch r.user u join fetch r.festival f where u.id= :userId",
            countQuery = "select count(r) from Review r where r.user.id= :userId")
    Page<Review> findByUserIdWithAll(Long userId, Pageable pageable);

    @Query(value = "select r from Review r where r.user.id= :userId and r.id= :reviewId")
    Optional<Review> findByUserIdAndId(Long userId, Long reviewId);

    @Modifying(clearAutomatically = true)
    @Query(value = "delete from Review r where r.user.id = :userId and r.id= :reviewId")
    int deleteByUserIdAndId(Long userId, Long reviewId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Review r where r.user.id = :userId")
    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Review r where r.festival.id = :festivalId")
    void deleteByFestivalId(Long festivalId);

    @Query("select r from Review r where r.festival.id = :festivalId")
    List<Review> findByFestivalId(Long festivalId);

    @Query("select r from Review r where r.user.id = :userId")
    List<Review> findByUserId(Long userId);
}
