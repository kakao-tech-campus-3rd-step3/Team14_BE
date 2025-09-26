package kakao.festapick.festival.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

public record FestivalUpdateRequestDto(

        @NotBlank
        String title,

        int areaCode,

        @NotBlank
        String addr1,

        String addr2,

        @NotNull
        FileUploadRequest posterInfo,

        List<FileUploadRequest> imageInfos,

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
