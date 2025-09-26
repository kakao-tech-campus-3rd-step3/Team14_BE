package kakao.festapick.user.service;

import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private CookieComponent cookieComponent;

    @Mock
    private S3Service s3Service;

    @Mock
    private TemporalFileRepository temporalFileRepository;

    @Test
    @DisplayName("프로필 이미지 변환 성공")
    void profileImageChangeSuccess() {

        // given
        UserEntity userEntity = new UserEntity("GOOGLE-1234",
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE);

        given(userLowService.findById(any()))
                .willReturn(userEntity);

        // when
        userService.changeProfileImage(userEntity.getId(), new FileUploadRequest(1L,"updateImageUrl"));

        // then
        verify(userLowService).findById(any());
        verify(s3Service).deleteS3File(any());
        verify(temporalFileRepository).deleteById(any());
        verifyNoMoreInteractions(userLowService,s3Service, temporalFileRepository,cookieComponent);

    }

    @Test
    @DisplayName("프로필 이미지 변환 실패 - 존재하지 않는 회원")
    void profileImageChangeFail() {

        // given

        given(userLowService.findById(any()))
                .willThrow(NotFoundEntityException.class);

        // when & then
        assertThatThrownBy(()->
                userService.changeProfileImage(1L, new FileUploadRequest(1L,"updateImageUrl"))
        ).isInstanceOf(NotFoundEntityException.class);
        verify(userLowService).findById(any());
        verifyNoMoreInteractions(userLowService,s3Service, temporalFileRepository,cookieComponent);

    }
}
