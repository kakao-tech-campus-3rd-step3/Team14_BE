package kakao.festapick.permission.fmpermission.dto;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.fmpermission.domain.FMPermission;

public record FMPermissionResponseDto(
        Long id,
        String department,
        LocalDateTime updatedDate,
        PermissionState state,
        List<String> docsUrls
)
{
    public FMPermissionResponseDto(FMPermission fmPermission, List<String> docsUrls){
        this(
                fmPermission.getId(),
                fmPermission.getDepartment(),
                fmPermission.getUpdatedDate(),
                fmPermission.getPermissionState(),
                docsUrls
        );
    }
}

