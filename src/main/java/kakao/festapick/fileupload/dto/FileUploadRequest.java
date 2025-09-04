package kakao.festapick.fileupload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kakao.festapick.fileupload.domain.TemporalFile;
import org.hibernate.validator.constraints.URL;

public record FileUploadRequest(
        @NotNull
        Long id,
        @URL(message = "url형식에 맞지 않습니다.")
        @NotBlank
        String presignedUrl
) {

        public FileUploadRequest(TemporalFile temporalFile) {
                this(temporalFile.getId(), temporalFile.getUrl());
        }
}
