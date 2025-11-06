package kakao.festapick.festival.dto;

import jakarta.validation.constraints.Size;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;

public record FestivalSearchCondForAdmin(
        @Size(max = 255, message = "축제 제목은 최대 255자 까지 가능합니다.")
        String title,
        FestivalState state,
        FestivalType festivalType
) {
}
