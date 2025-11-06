package kakao.festapick.chat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record ChatRequestDto(
        @NotBlank
        @Max(value = 255, message = "채팅은 최대 255자 까지 작성 가능합니다.")
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
