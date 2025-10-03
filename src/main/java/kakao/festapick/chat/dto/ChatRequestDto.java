package kakao.festapick.chat.dto;

import jakarta.validation.constraints.NotBlank;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record ChatRequestDto(
        @NotBlank
        String content,
        FileUploadRequest imageInfo
) {

}
