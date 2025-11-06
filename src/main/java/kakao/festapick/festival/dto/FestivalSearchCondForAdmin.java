package kakao.festapick.festival.dto;

import jakarta.validation.constraints.Size;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;

public record FestivalSearchCondForAdmin(
        String title,
        FestivalState state,
        FestivalType festivalType
) {
}
