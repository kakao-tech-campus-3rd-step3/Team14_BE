package kakao.festapick.permission.fmpermission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record FMPermissionRequestDto(
        @NotBlank(message = "소속 기관 입력은 필수입니다.")
        String department,

        @Size(min = 1,message = "최소 1개 이상의 서류를 업로드해야합니다.")
        List<@Valid FileUploadRequest> documents
)
{ }
