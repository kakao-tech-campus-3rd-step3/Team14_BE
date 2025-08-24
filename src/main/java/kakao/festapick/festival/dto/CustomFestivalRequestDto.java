package kakao.festapick.festival.dto;

import java.time.LocalDate;
import org.antlr.v4.runtime.misc.NotNull;

public record CustomFestivalRequestDto(
        String contentId,
        @NotNull
        String title,
        String areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        String homePage,
        String overView
) {

}
