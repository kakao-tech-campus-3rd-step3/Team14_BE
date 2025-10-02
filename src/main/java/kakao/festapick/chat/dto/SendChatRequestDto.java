package kakao.festapick.chat.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record SendChatRequestDto(
        @NotBlank
        String content,
        List<FileUploadRequest> imageInfos
) {
        public SendChatRequestDto (String content){
                this(content, null);
        }

        public SendChatRequestDto {
                if (imageInfos == null) imageInfos = new ArrayList<>();
        }
}
