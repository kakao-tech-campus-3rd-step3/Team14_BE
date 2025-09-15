package kakao.festapick.wish.repository;

import java.util.Optional;
import kakao.festapick.wish.domain.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    @Query(value = "select w from Wish w join w.user u on u.identifier= :identifier join fetch w.festival f",
            countQuery = "select count(w) from Wish w join w.user u on u.identifier= :identifier")
    Page<Wish> findByUserIdentifier(String identifier, Pageable pageable);

    Optional<Wish> findByUserIdentifierAndFestivalId(String identifier, Long festivalId);

    Optional<Wish> findByUserIdentifierAndId(String identifier, Long wishId);

    @Modifying
    @Query("delete from Wish w where w.user.id = :userId")
    void deleteByUserId(Long userId);
}
