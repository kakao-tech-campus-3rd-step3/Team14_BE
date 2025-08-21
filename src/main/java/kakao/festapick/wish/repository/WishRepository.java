package kakao.festapick.wish.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.wish.domain.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    @EntityGraph(attributePaths = {"festival"})
    Page<Wish> findByUserIdentifier(String identifier, Pageable pageable);

    Optional<Wish> findByUserIdentifierAndFestivalId(String identifier, Long festivalId);

    Optional<Wish> findByUserIdentifierAndId(String identifier, Long wishId);
}
