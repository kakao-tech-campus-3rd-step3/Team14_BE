package kakao.festapick.admin.dto;

import kakao.festapick.user.domain.UserRoleType;

public record UserSearchCond(
        String identifier,
        String email,
        UserRoleType role
) {
}
