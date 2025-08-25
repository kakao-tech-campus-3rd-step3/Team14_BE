package kakao.festapick.festival.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CustomFestivalRequestDto(
        @NotBlank
        String title,
        int areaCode,
        String addr1,
        String addr2,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        String homePage,
        String overView
) {

}
