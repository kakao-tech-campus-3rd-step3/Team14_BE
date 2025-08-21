package kakao.festapick.wish.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.wish.domain.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    List<Wish> findByUser_Id(Long userId);

    Optional<Wish> findByUser_IdAndId(Long userId, Long id);

    Optional<Wish> findByUser_IdAndFestival_Id(Long userId, Long festivalId);
}
