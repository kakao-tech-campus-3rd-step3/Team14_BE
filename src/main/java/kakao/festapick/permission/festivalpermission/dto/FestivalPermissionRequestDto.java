package kakao.festapick.permission.festivalpermission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record FestivalPermissionRequestDto(
        @Size(min = 1,message = "최소 1개 이상의 서류를 업로드해야합니다.")
        List<@Valid FileUploadRequest> documents
)
{

}
