package kakao.festapick.festival.dto;

import java.time.LocalDate;

public record FestivalRequestDto(
        String contentId,
        String title,
        String areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        String homePage,
        String overView
){
    public FestivalRequestDto(
            String contentId,
            String title,
            String areaCode,
            String addr1,
            String addr2,
            String imageUrl,
            LocalDate startDate,
            LocalDate endDate
    )
    {
        this(contentId, title, areaCode, addr1, addr2, imageUrl, startDate, endDate, "homePage", "overView");
    }

}
