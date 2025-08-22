package kakao.festapick.festival.dto;

import jakarta.persistence.Column;

public record FestivalRequestDto(
        String contentId,
        String title,
        String areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        String startDate,
        String endDate,
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
            String startDate,
            String endDate
    )
    {
        this(contentId, title, areaCode, addr1, addr2, imageUrl, startDate, endDate, "homePage", "overView");
    }

}
