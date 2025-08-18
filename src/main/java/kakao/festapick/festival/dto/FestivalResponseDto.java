package kakao.festapick.festival.dto;

import jakarta.persistence.Column;
import kakao.festapick.festival.domain.Festival;

public record FestivalResponseDto(
        Long id,
        String contentId,
        String title,
        String areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        String startDate,
        String endDate,
        @Column(length = 5000)
        String overView,
        String homePage
) {

    public FestivalResponseDto(Festival festival){
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
