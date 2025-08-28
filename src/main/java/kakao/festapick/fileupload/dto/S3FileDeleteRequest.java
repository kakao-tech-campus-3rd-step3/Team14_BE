package kakao.festapick.fileupload.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record S3FileDeleteRequest(
        @NotNull
        List<String> presignedUrls
) {
}
