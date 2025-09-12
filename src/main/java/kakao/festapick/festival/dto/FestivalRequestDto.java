package kakao.festapick.festival.dto;

import java.time.LocalDate;

public record FestivalRequestDto(
        String contentId,
        String title,
        int areaCode,
        String addr1,
        String addr2,
        String posterInfo,
        LocalDate startDate,
        LocalDate endDate,
        String homePage,
        String overView
){
    public FestivalRequestDto(
            String contentId,
            String title,
            int areaCode,
            String addr1,
            String addr2,
            String posterInfo,
            LocalDate startDate,
            LocalDate endDate
    )
    {
        this(contentId, title, areaCode, addr1, addr2, posterInfo, startDate, endDate, "homePage", "overView");
    }

}
