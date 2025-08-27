package kakao.festapick.festival.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.hibernate.validator.constraints.Length;

public record FestivalUpdateRequestDto(

        @NotBlank
        String title,

        int areaCode,

        @NotBlank
        String addr1,

        String addr2,

        String imageUrl,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        String homePage,

        @NotBlank
        @Length(min = 30, max = 5000)
        String overView
)
{}
