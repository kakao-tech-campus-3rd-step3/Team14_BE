package kakao.festapick.festival.dto;

import java.time.LocalDate;
import kakao.festapick.festival.domain.Festival;

public record FestivalDetailResponse(
        Long id,
        String contentId,
        String title,
        int areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        String overView,
        String homePage
) {

    public FestivalDetailResponse(Festival festival){
        this(
                festival.getId(),
                festival.getContentId(),
                festival.getTitle(),
                festival.getAreaCode(),
                festival.getAddr1(),
                festival.getAddr2(),
                festival.getImageUrl(),
                festival.getStartDate(),
                festival.getEndDate(),
                festival.getOverView(),
                festival.getHomePage()
        );
    }
}
