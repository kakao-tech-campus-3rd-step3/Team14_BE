package kakao.festapick.dto;

public record ApiResponseDto<T>(
        T content
) {
}
