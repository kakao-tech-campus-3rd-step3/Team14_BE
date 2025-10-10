package kakao.festapick.user.dto;

import kakao.festapick.user.domain.UserEntity;

public record UserResponseDto(
        Long userId,
        String email,
        String username,
        String profileImageUrl
) {

    public UserResponseDto(UserEntity userEntity) {
        this(userEntity.getId(), userEntity.getEmail(), userEntity.getUsername(), userEntity.getProfileImageUrl());
    }
}
