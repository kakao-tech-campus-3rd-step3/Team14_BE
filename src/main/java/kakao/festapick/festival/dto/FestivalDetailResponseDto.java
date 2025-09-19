package kakao.festapick.festival.dto;

import java.time.LocalDate;
import java.util.List;
import kakao.festapick.festival.domain.Festival;

public record FestivalDetailResponseDto(
        Long id,
        String contentId,
        String title,
        int areaCode,
        String addr1,
        String addr2,
        String posterInfo,
        LocalDate startDate,
        LocalDate endDate,
        String overView,
        String homePage,
        List<String> imageInfos
) {
    public FestivalDetailResponseDto(Festival festival){
        this(
                festival.getId(),
                festival.getContentId(),
                festival.getTitle(),
                festival.getAreaCode(),
                festival.getAddr1(),
                festival.getAddr2(),
                festival.getPosterInfo(),
                festival.getStartDate(),
                festival.getEndDate(),
                festival.getOverView(),
                festival.getHomePage(),
                null
        );
    }

    public FestivalDetailResponseDto(Festival festival, List<String> images){
        this(
                festival.getId(),
                festival.getContentId(),
                festival.getTitle(),
                festival.getAreaCode(),
                festival.getAddr1(),
                festival.getAddr2(),
                festival.getPosterInfo(),
                festival.getStartDate(),
                festival.getEndDate(),
                festival.getOverView(),
                festival.getHomePage(),
                images
        );
    }
}
