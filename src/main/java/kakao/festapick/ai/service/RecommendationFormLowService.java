package kakao.festapick.ai.service;

import kakao.festapick.ai.domain.RecommendationForm;
import kakao.festapick.ai.repository.RecommendationFormRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class RecommendationFormLowService {

    private final RecommendationFormRepository recommendationFormRepository;

    public RecommendationForm findByUserId(Long userId) {
        return recommendationFormRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundEntityException(ExceptionCode.RECOMMENDATION_FORM_NOT_FOUND));
    }

    public RecommendationForm save(RecommendationForm recommendationForm) {
        return recommendationFormRepository.save(recommendationForm);
    }

    public void deleteByUserId(Long userId) {
        recommendationFormRepository.deleteByUserId(userId);
    }
}
