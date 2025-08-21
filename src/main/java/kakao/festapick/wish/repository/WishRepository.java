package kakao.festapick.wish.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.wish.domain.Wish;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    @EntityGraph(attributePaths = {"festival"})
    List<Wish> findByUserIdentifier(String identifier);

    Optional<Wish> findByUserIdentifierAndId(String identifier, Long id);

    Optional<Wish> findByUserIdentifierAndFestivalId(String identifier, Long festivalId);
}
