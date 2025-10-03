package kakao.festapick.chat.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record ChatRequestDto(
        @NotBlank
        String content,
        List<FileUploadRequest> imageInfos
) {

        public ChatRequestDto {
                if (imageInfos == null) imageInfos = new ArrayList<>();
        }
}
