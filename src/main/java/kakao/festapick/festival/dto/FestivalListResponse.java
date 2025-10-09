package kakao.festapick.festival.dto;

import kakao.festapick.festival.domain.Festival;

import java.time.LocalDate;

public record FestivalListResponse(
        Long id,
        Long managerId,
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
                festival.getManager() == null ? null : festival.getManager().getId(),
                festival.getTitle(),
                festival.getAddr1(),
                festival.getAddr2(),
                festival.getPosterInfo(),
                festival.getStartDate(),
                festival.getEndDate()
        );
    }
}
