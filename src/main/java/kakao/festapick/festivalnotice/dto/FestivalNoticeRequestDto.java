package kakao.festapick.festivalnotice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record FestivalNoticeRequestDto(
        @NotBlank
        @Max(value = 100, message = "공지사항 제목은 최대 100자입니다.")
        String title,

        @NotBlank
        @Max(value = 2000, message = "공지사항 내용은 최대 2000자입니다.")
        String content,

        @Size(max = 10, message = "공지사항 사진은 최대 10장 업로드 가능합니다.")
        List<@Valid FileUploadRequest> images
) {
}
