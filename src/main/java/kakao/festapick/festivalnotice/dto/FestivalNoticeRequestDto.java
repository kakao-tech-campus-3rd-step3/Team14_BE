package kakao.festapick.festivalnotice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record FestivalNoticeRequestDto(
        @NotBlank
        String title,

        @NotBlank
        String content,

        List<@Valid FileUploadRequest> images
) {
}
