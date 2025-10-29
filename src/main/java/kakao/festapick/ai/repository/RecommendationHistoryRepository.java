package kakao.festapick.ai.repository;

import kakao.festapick.ai.domain.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {

    @Query("select rh from RecommendationHistory rh join fetch rh.festival where rh.user.id = :userId")
    List<RecommendationHistory> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from RecommendationHistory rh where rh.user.id = :userId")
    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from RecommendationHistory rh where rh.festival.id = :festivalId")
    void deleteByFestivalId(Long festivalId);



}
