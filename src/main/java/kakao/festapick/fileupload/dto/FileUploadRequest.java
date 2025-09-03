package kakao.festapick.fileupload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kakao.festapick.fileupload.domain.TemporalFile;

public record FileUploadRequest(
        @NotNull
        Long id,
        @NotBlank
        String presignedUrl
) {

        public FileUploadRequest(TemporalFile temporalFile) {
                this(temporalFile.getId(), temporalFile.getUrl());
        }
}
