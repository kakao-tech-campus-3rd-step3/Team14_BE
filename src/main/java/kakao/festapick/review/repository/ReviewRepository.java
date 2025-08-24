package kakao.festapick.review.repository;

import java.util.Optional;
import kakao.festapick.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "select (count(r) > 0) from Review r where r.user.id= :userId and r.festival.id= :festivalId")
    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    @Query(value = "select r from Review r join fetch r.festival f join fetch r.user u where f.id= :festivalId",
            countQuery = "select count(r) from Review r where r.festival.id= :festivalId")
    Page<Review> findByFestivalId(Long festivalId, Pageable pageable);

    @Query(value = "select r from Review r join fetch r.user u join fetch r.festival f where u.identifier= :identifier",
            countQuery = "select count(r) from Review r where r.user.identifier= :identifier")
    Page<Review> findByUserIdentifier(String identifier, Pageable pageable);

    @Query(value = "select r from Review r where r.user.identifier= :identifier and r.id= :reviewId")
    Optional<Review> findByUserIdentifierAndId(String identifier, Long reviewId);
}
