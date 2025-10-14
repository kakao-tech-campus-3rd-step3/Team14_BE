package kakao.festapick.permission.festivalpermission.dto;

import java.time.LocalDateTime;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;

public record FestivalPermissionAdminListDto(
        Long id,
        String email,
        String name,
        String department,
        String festivalTitle,
        LocalDateTime appliedDate,
        PermissionState permissionState
){
    public FestivalPermissionAdminListDto(FestivalPermission festivalPermission, String department){
        this(
                festivalPermission.getId(),
                festivalPermission.getUser().getEmail(),
                festivalPermission.getUser().getUsername(),
                department,
                festivalPermission.getFestival().getTitle(),
                festivalPermission.getUpdatedDate(),
                festivalPermission.getPermissionState()
        );
    }

}
