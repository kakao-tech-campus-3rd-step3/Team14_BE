package kakao.festapick.wish.dto;

import kakao.festapick.festival.dto.FestivalDetailResponse;

public record WishResponseDto(Long wishId,
                              Long festivalId,
                              String title,
                              String areaCode) {

}
