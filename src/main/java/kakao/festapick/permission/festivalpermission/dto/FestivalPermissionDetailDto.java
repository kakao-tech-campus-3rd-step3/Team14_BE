package kakao.festapick.permission.festivalpermission.dto;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;

public record FestivalPermissionDetailDto (
        Long id,
        String title,
        String posterImg,
        LocalDateTime appliedDate,
        PermissionState state,
        List<String> docs
)
{
    public FestivalPermissionDetailDto(FestivalPermission festivalPermission, List<String> docs){
        this(
                festivalPermission.getId(),
                festivalPermission.getFestival().getTitle(),
                festivalPermission.getFestival().getPosterInfo(),
                festivalPermission.getUpdatedDate(),
                festivalPermission.getPermissionState(),
                docs
        );
    }
}
