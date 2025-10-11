package kakao.festapick.wish.dto;

public record WishResponseDto(Long wishId,
                              Long festivalId,
                              Long userId,
                              String title,
                              int areaCode) {

}
