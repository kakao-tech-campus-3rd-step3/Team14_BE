package kakao.festapick.festival.dto;

import java.time.LocalDateTime;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.permission.PermissionState;

public record FestivalCustomListResponse(
        Long id,
        String title,
        LocalDateTime updatedDate,
        FestivalState state
)
{
    public FestivalCustomListResponse(Festival festival){
        this(
                festival.getId(),
                festival.getTitle(),
                festival.getUpdatedDate(),
                festival.getState()
        );
    }
}
