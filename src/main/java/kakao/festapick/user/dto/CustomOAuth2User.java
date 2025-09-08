package kakao.festapick.user.dto;

import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserRoleType userRoleType;
    private final String identifier;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Map<String,Object> attributes, UserEntity user) {
        this.userRoleType = user.getRoleType();
        this.identifier = user.getIdentifier();
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userRoleType.name();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return this.identifier;
    }

}
