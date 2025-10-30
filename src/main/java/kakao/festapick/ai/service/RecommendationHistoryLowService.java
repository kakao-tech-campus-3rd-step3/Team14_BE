package kakao.festapick.ai.service;

import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.repository.RecommendationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class RecommendationHistoryLowService {

    private final RecommendationHistoryRepository recommendationHistoryRepository;

    public void saveAll(List<RecommendationHistory> recommendationHistories) {
        recommendationHistoryRepository.saveAll(recommendationHistories);
    }

    public List<RecommendationHistory> findByUserIdWithFestival(Long userId) {
        return recommendationHistoryRepository.findByUserIdWithFestival(userId);
    }

    public void deleteByUserId(Long userId) {
        recommendationHistoryRepository.deleteByUserId(userId);
    }

    public void deleteByFestivalId(Long festivalId) {
        recommendationHistoryRepository.deleteByFestivalId(festivalId);
    }
}
