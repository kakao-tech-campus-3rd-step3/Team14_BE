package kakao.festapick.config;

import jakarta.annotation.PostConstruct;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.global.DefaultImageProperties;
import kakao.festapick.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        DefaultImageProperties.class
})
@RequiredArgsConstructor
public class DefaultImageConfig {

    private final DefaultImageProperties defaultImageProperties;

    @PostConstruct
    public void init() {
        Festival.setDefaultImage(defaultImageProperties.getFestival());
        UserEntity.setDefaultImage(defaultImageProperties.getProfile());
    }
}
