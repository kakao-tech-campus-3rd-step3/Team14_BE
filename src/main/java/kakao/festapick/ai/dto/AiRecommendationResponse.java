package kakao.festapick.ai.dto;

import java.time.LocalDate;

public record AiRecommendationResponse(
        Long id,
        Long managerId,
        String title,
        String addr1,
        String addr2,
        String posterInfo,
        LocalDate startDate,
        LocalDate endDate
) {
}
