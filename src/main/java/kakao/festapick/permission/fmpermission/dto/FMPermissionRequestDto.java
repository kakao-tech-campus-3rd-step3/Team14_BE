package kakao.festapick.permission.fmpermission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import kakao.festapick.fileupload.dto.FileUploadRequest;

public record FMPermissionRequestDto(
        @NotBlank(message = "소속 기관 입력은 필수입니다.")
        @Max(value = 50, message = "소속 기관은 최대 50자 입력가능합니다.")
        @Min(value = 2, message = "소속 기관은 최소 2자 이상 입력해야합니다.")
        String department,

        @Size(min = 1,message = "최소 1개 이상의 서류를 업로드해야합니다.")
        List<@Valid FileUploadRequest> documents
)
{ }
