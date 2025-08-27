package kakao.festapick.wish.dto;

public record WishResponseDto(Long wishId,
                              Long festivalId,
                              String title,
                              int areaCode) {

}
