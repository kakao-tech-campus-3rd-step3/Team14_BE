package kakao.festapick.ai.service;

import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.repository.RecommendationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class RecommendationHistoryLowService {

    private final RecommendationHistoryRepository recommendationHistoryRepository;


    public List<RecommendationHistory> findByFestivalsIdsAndUserId(Set<Long> festivalIds, Long userId) {
        return recommendationHistoryRepository.findByFestivalIdsAndUserId(festivalIds, userId);
    }

    public void saveAll(List<RecommendationHistory> recommendationHistories) {
        recommendationHistoryRepository.saveAll(recommendationHistories);
    }
}
