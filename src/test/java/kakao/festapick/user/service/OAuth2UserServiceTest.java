package kakao.festapick.user.service;


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

    @Mock
    private CookieComponent cookieComponent;

    @Mock
    private S3Service s3Service;


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
        verifyNoMoreInteractions(cookieComponent);
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
        verifyNoMoreInteractions(cookieComponent);

    }

    @Test
    @DisplayName("프로필 이미지 변환 성공")
    void profileImageChangeSuccess() {

        // given
        UserEntity userEntity = new UserEntity("GOOGLE-1234",
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE);

        given(userRepository.findByIdentifier(any()))
                .willReturn(Optional.of(userEntity));

        // when
        oAuth2UserService.changeProfileImage(userEntity.getIdentifier(), "updateImageUrl");

        // then
        verify(userRepository).findByIdentifier(any());
        verify(s3Service).deleteS3File(any());
        verifyNoMoreInteractions(userRepository,s3Service);

    }

    @Test
    @DisplayName("프로필 이미지 변환 실패 - 존재하지 않는 회원")
    void profileImageChangeFail() {

        // given

        given(userRepository.findByIdentifier(any()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(()->
                oAuth2UserService.changeProfileImage("GOOGLE-1234", "updateImageUrl")
        ).isInstanceOf(NotFoundEntityException.class);
        verify(userRepository).findByIdentifier(any());
        verifyNoMoreInteractions(userRepository,s3Service);

    }

    private GoogleOAuth2Rep socialLoginCase() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1234");
        attributes.put("email", "example@gmail.com");
        attributes.put("name", "exampleName");

        return new GoogleOAuth2Rep(attributes);
    }

}