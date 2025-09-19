package kakao.festapick.festival.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import org.hibernate.validator.constraints.Length;

public record FestivalCustomRequestDto(
        @NotBlank
        String title,

        int areaCode,

        @NotBlank
        String addr1,

        String addr2,

        //poster - 포스터는 한장만(필수)
        @NotNull
        FileUploadRequest posterInfo,

        // 축제 관련 이미지는 여러장 업로드 가능
        List<FileUploadRequest> imageInfos,

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
