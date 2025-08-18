package kakao.festapick.user.dto;

import kakao.festapick.user.domain.UserEntity;

public record UserResponseDto(
        Long id,
        String email,
        String identifier,
        String username,
        String role
) {
    public UserResponseDto(UserEntity userEntity) {
        this(userEntity.getId(), userEntity.getEmail(),
                userEntity.getIdentifier(), userEntity.getUsername(), userEntity.getRoleType().name());
    }
}
