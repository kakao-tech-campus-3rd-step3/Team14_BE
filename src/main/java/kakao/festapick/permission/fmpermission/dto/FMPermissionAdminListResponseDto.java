package kakao.festapick.permission.fmpermission.dto;

import java.time.LocalDateTime;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.fmpermission.domain.FMPermission;

public record FMPermissionAdminListResponseDto(
        Long id,
        Long userId,
        String email,
        String name,
        LocalDateTime appliedDate,
        PermissionState permissionState
)
{
    public FMPermissionAdminListResponseDto(FMPermission fmPermission){
        this(
                fmPermission.getId(),
                fmPermission.getUser().getId(),
                fmPermission.getUser().getEmail(),
                fmPermission.getUser().getUsername(),
                fmPermission.getUpdatedDate(),
                fmPermission.getPermissionState()
        );
    }
}
