package kakao.festapick.user.dto;

import kakao.festapick.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final String role;
    private final String username;
    private final String identifier;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Map<String,Object> attributes, User user) {
        this.role = user.getRoleType().name();
        this.username = user.getUsername();
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
                return role;
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return this.identifier;
    }

}
