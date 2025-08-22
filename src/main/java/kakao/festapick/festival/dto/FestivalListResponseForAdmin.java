package kakao.festapick.festival.dto;

import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;

public record FestivalListResponseForAdmin(
        Long id,
        String title,
        FestivalState state
) {

    public FestivalListResponseForAdmin(Festival festival) {
        this(festival.getId(), festival.getTitle(), festival.getState());
    }
}
