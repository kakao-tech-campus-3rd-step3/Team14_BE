package kakao.festapick.permission.festivalpermission.dto;

import java.time.LocalDateTime;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;

public record FestivalPermissionResponseListDto(
        Long id,
        String title,
        LocalDateTime appliedDate,
        PermissionState state
){

    public FestivalPermissionResponseListDto(FestivalPermission festivalPermission){
        this(
                festivalPermission.getId(),
                festivalPermission.getFestival().getTitle(),
                festivalPermission.getUpdatedDate(),
                festivalPermission.getPermissionState()
        );
    }

}
