package kakao.festapick.ai.repository;

import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.user.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {

    @Query("select rh from RecommendationHistory rh where rh.festival.id in (:festivalIds) and rh.user.id = :userId")
    List<RecommendationHistory> findByFestivalIdsAndUserId(Set<Long> festivalIds, Long userId);

    @Query("select rh from RecommendationHistory rh join fetch rh.festival where rh.user.id = :userId order by rh.createdDate desc")
    Page<RecommendationHistory> findByUserIdWithFestival(Long userId, Pageable pageable);
}
