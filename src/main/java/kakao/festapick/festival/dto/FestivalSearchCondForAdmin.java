package kakao.festapick.festival.dto;

import kakao.festapick.festival.domain.FestivalState;

public record FestivalSearchCondForAdmin(
        String title,
        FestivalState state
) {
}
