package kakao.festapick.festival.dto;

import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;

public record FestivalListResponseForAdmin(
        Long id,
        String title,
        FestivalState state,
        FestivalType festivalType
) {

    public FestivalListResponseForAdmin(Festival festival) {
        this(festival.getId(), festival.getTitle(), festival.getState(), festival.getFestivalType());
    }
}
