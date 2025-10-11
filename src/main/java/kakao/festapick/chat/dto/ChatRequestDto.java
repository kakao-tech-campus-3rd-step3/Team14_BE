package kakao.festapick.chat.dto;

import jakarta.validation.constraints.NotBlank;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record ChatRequestDto(
        @NotBlank
        String content,
        FileUploadRequest imageInfo
) {

    public String getImageUrl() {
        return imageInfo.presignedUrl();
    }

    public Long getTemporalFileId() {
        return imageInfo.id();
    }

}
