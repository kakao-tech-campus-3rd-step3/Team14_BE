package kakao.festapick.ai.repository;

import kakao.festapick.ai.domain.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {

    @Query("select rh from RecommendationHistory rh where rh.festival.id in (:festivalIds) and rh.user.id = :userId")
    List<RecommendationHistory> findByFestivalIdsAndUserId(Set<Long> festivalIds, Long userId);
}
