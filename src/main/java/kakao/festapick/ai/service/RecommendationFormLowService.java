package kakao.festapick.ai.service;

import kakao.festapick.ai.domain.RecommendationForm;
import kakao.festapick.ai.repository.RecommendationFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class RecommendationFormLowService {

    private final RecommendationFormRepository recommendationFormRepository;

    public RecommendationForm save(RecommendationForm recommendationForm) {
        return recommendationFormRepository.save(recommendationForm);
    }

    public void deleteByUserId(Long userId) {
        recommendationFormRepository.deleteByUserId(userId);
    }
}
