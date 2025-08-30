package kakao.festapick.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileImageUpdateRequest(
        @NotBlank
        String imageUrl
) {
}
