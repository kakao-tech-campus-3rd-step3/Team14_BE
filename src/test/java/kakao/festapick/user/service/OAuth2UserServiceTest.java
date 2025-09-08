package kakao.festapick.user.service;


import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.GoogleOAuth2Rep;
import kakao.festapick.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    @Mock
    private UserRepository userRepository;


    @Test
    @DisplayName("소셜 로그인 성공(회원가입)")
    void socialLoginSuccessCase1() {

        // given
        GoogleOAuth2Rep user = socialLoginCase();

        String identifier = user.getProvider() + "_" + user.getProviderId();

        given(userRepository.findByIdentifier(anyString()))
                .willReturn(Optional.empty());

        given(userRepository.save(any()))
                .willReturn(new UserEntity(identifier, user.getEmail(),
                        user.getUsername(), UserRoleType.USER, user.getProvider()));

        // when

        UserEntity userEntity = oAuth2UserService.socialLogin(user);

        // then

        assertSoftly(
                softly-> {
                    softly.assertThat(userEntity.getIdentifier()).isEqualTo(identifier);
                    softly.assertThat(userEntity.getEmail()).isEqualTo(user.getEmail());
                    softly.assertThat(userEntity.getUsername()).isEqualTo(user.getUsername());
                    softly.assertThat(userEntity.getRoleType()).isEqualTo(UserRoleType.USER);
                }
        );

        verify(userRepository).findByIdentifier(anyString());
        verify(userRepository).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("소셜 로그인 성공(이미 회원가입되어있던 회원)")
    void socialLoginSuccessCase2() {
        // given
        GoogleOAuth2Rep user = socialLoginCase();

        String identifier = user.getProvider() + "_" + user.getProviderId();

        given(userRepository.findByIdentifier(anyString()))
                .willReturn(Optional.of(new UserEntity(identifier, user.getEmail(),
                        user.getUsername(), UserRoleType.USER, user.getProvider())));


        // when

        UserEntity userEntity = oAuth2UserService.socialLogin(user);

        // then

        assertSoftly(
                softly-> {
                    softly.assertThat(userEntity.getIdentifier()).isEqualTo(identifier);
                    softly.assertThat(userEntity.getEmail()).isEqualTo(user.getEmail());
                    softly.assertThat(userEntity.getUsername()).isEqualTo(user.getUsername());
                    softly.assertThat(userEntity.getRoleType()).isEqualTo(UserRoleType.USER);
                }
        );

        verify(userRepository).findByIdentifier(anyString());
        verifyNoMoreInteractions(userRepository);

    }

    private GoogleOAuth2Rep socialLoginCase() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1234");
        attributes.put("email", "example@gmail.com");
        attributes.put("name", "exampleName");

        return new GoogleOAuth2Rep(attributes);
    }

}
