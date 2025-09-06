package kakao.festapick.festival.dto;

import java.time.LocalDate;
import kakao.festapick.festival.domain.Festival;

public record FestivalListResponse(
        Long id,
        String title,
        String addr1,
        String addr2,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate
) {

    public FestivalListResponse(Festival festival){
        this(
                festival.getId(),
                festival.getTitle(),
                festival.getAddr1(),
                festival.getAddr2(),
                festival.getImageUrl(),
                festival.getStartDate(),
                festival.getEndDate()
        );
    }
}
