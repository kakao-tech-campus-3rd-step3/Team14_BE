package kakao.festapick.festival.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import kakao.festapick.fileupload.dto.FileUploadRequest;
import org.hibernate.validator.constraints.Length;

public record FestivalCustomRequestDto(
        @NotBlank
        String title,

        int areaCode,

        @NotBlank
        String addr1,

        String addr2,

        FileUploadRequest imageInfo,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        String homePage,

        @NotNull
        @Length(min = 30, max = 5000)
        String overView
) {
}
