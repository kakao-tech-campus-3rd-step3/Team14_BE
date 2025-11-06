package kakao.festapick.festival.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

public record FestivalCustomRequestDto(
        @NotBlank
        @Max(value = 255, message = "축제 제목은 최대 255자 까지 가능합니다.")
        String title,

        int areaCode,

        @NotBlank
        @Max(value = 255, message = "주소는 최대 255자 까지 가능합니다.")
        String addr1,

        @Max(value = 255, message = "상세 주소는 최대 255자 까지 가능합니다.")
        String addr2,

        //poster - 포스터는 한장만(필수)
        @NotNull
        @Valid
        FileUploadRequest posterInfo,

        // 축제 관련 이미지는 여러장 업로드 가능
        List<@Valid FileUploadRequest> imageInfos,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Max(value = 500, message = "홈페이지 주소는 최대 500자 까지 가능합니다.")
        String homePage,

        @NotNull
        @Length(min = 30, max = 5000, message = "축제 개요는 최소 30자, 최대 5000자 까지 가능합니다.")
        String overView
) {

}
