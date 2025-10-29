package kakao.festapick.ai.repository;

import kakao.festapick.ai.domain.RecommendationForm;
import kakao.festapick.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RecommendationFormRepository extends JpaRepository<RecommendationForm, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from RecommendationForm rf where rf.user.id = :userId")
    void deleteByUserId(Long userId);

    @Query("select rf from RecommendationForm  rf where rf.user.id = :userId")
    Optional<RecommendationForm> findByUserId(Long userId);

    Long user(UserEntity user);
}
