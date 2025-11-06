package kakao.festapick.festival.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

public record FestivalUpdateRequestDto(

        @NotBlank
        @Size(max = 255, message = "축제 제목은 최대 255자 까지 가능합니다.")
        String title,

        int areaCode,

        @NotBlank
        @Size(max = 255, message = "주소는 최대 255자 까지 가능합니다.")
        String addr1,

        @Size(max = 255, message = "상세 주소는 최대 255자 까지 가능합니다.")
        String addr2,

        @NotNull
        @Valid
        FileUploadRequest posterInfo,

        @Size(max = 10, message = "사진은 최대 10장 까지 업로드 가능합니다.")
        List<@Valid FileUploadRequest> imageInfos,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Size(max = 500, message = "홈페이지 주소는 최대 500자 까지 가능합니다.")
        String homePage,

        @NotBlank
        @Size(min = 30, max = 5000, message = "축제 개요는 최소 30자 최대 5000자 까지 가능합니다.")
        String overView
)
{}
