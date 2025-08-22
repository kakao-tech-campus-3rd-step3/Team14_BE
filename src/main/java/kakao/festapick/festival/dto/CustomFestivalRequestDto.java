package kakao.festapick.festival.dto;

import jakarta.persistence.Column;
import org.antlr.v4.runtime.misc.NotNull;

public record CustomFestivalRequestDto(
        String contentId,
        @NotNull
        String title,
        String areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        String startDate,
        String endDate,
        String homePage,
        String overView
) {

}
