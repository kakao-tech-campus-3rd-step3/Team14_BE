package kakao.festapick.festival.dto;

import java.time.LocalDate;
import kakao.festapick.festival.domain.Festival;

public record FestivalListResponse(
        Long id,
        String title,
        String addr1,
        String addr2,
        String posterInfo,
        LocalDate startDate,
        LocalDate endDate
) {

    public FestivalListResponse(Festival festival){
        this(
                festival.getId(),
                festival.getTitle(),
                festival.getAddr1(),
                festival.getAddr2(),
                festival.getPosterInfo(),
                festival.getStartDate(),
                festival.getEndDate()
        );
    }
}
