package kakao.festapick.user.service;

import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.*;
import kakao.festapick.user.repository.QUserRepository;
import kakao.festapick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService  {

    private final UserRepository userRepository;
    private final CookieComponent cookieComponent;
    private final QUserRepository qUserRepository;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2Response oAuth2Response = parseOAuth2User(userRequest, oAuth2User);

        UserEntity user = socialLogin(oAuth2Response);

        return new CustomOAuth2User(oAuth2User.getAttributes(), user);
    }

    public UserEntity socialLogin(OAuth2Response oAuth2Response) {
        String identifier = oAuth2Response.getProvider() + "-" +oAuth2Response.getProviderId();
        Optional<UserEntity> findUser = userRepository.findByIdentifier(identifier);

        if (findUser.isPresent()) return findUser.get();
        return userRepository.save(new UserEntity(identifier, oAuth2Response.getEmail(), oAuth2Response.getUsername(), UserRoleType.USER, oAuth2Response.getProvider()));
    }

    public UserEntity findByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier)
                .orElseThrow(()->new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
    }

    public void withDraw(String identifier, HttpServletResponse response) {
        userRepository.deleteByIdentifier(identifier);
        response.setHeader("Set-Cookie", cookieComponent.deleteRefreshToken());
    }

    public Page<UserResponseDtoForAdmin> findByIdentifierOrUserEmail(UserSearchCond userSearchCond, Pageable pageable) {
        return qUserRepository.findByIdentifierOrUserEmail(userSearchCond, pageable)
                .map(UserResponseDtoForAdmin::new);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void changeUserRole(Long id, UserRoleType role) {
        UserEntity findUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
        findUser.changeUserRole(role);
    }

    public void changeProfileImage(String identifier, FileUploadRequest fileUploadRequest) {
        UserEntity findUser = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));

        s3Service.deleteS3File(findUser.getProfileImageUrl());
        findUser.changeProfileImage(fileUploadRequest.presignedUrl());
        temporalFileRepository.deleteById(fileUploadRequest.id());
    }

    public UserResponseDto findMyInfo(String identifier) {
        UserEntity findUser = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));

        return new UserResponseDto(findUser);
    }

    private OAuth2Response parseOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String social = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        if (social.equalsIgnoreCase(SocialType.KAKAO.name()) || social.equalsIgnoreCase("kakao-ssr")) {
            return new KakaoOAuth2Rep(oAuth2User.getAttributes());

        }
        else if (social.equalsIgnoreCase(SocialType.GOOGLE.name())) {
            return new GoogleOAuth2Rep(oAuth2User.getAttributes());
        }
        else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜로그인 입니다.");
        }
    }
}
