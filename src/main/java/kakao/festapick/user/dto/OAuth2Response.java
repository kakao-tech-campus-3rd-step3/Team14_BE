package kakao.festapick.user.dto;

import kakao.festapick.user.domain.SocialType;

public interface OAuth2Response {
    SocialType getProvider();
    String getProviderId();
    String getUsername();
    String getEmail();
}
