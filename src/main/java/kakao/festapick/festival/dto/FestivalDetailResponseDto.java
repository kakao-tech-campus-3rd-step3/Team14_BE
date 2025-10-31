package kakao.festapick.festival.dto;

import kakao.festapick.festival.domain.Festival;

import java.time.LocalDate;
import java.util.List;
import kakao.festapick.festival.domain.FestivalState;

public record FestivalDetailResponseDto(
        Long id,
        Long managerId,
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
        List<String> imageInfos,
        FestivalState state,
        Double averageScore,
        long wishCount,
        boolean isMyWish
) {
    public FestivalDetailResponseDto(Festival festival, List<String> images, Double averageScore, long wishCount, boolean isMyWish) {
        this(
                festival.getId(),
                festival.getManager() == null ? null : festival.getManager().getId(),
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
                images,
                festival.getState(),
                averageScore,
                wishCount,
                isMyWish
        );
    }
}
