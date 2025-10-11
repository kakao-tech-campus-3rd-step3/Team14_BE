package kakao.festapick.global.dto;

public record ApiResponseDto<T>(
        T content
) {
}
