package kakao.festapick.user.service;

import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.dto.UserResponseDtoForAdmin;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.user.repository.QUserRepository;
import kakao.festapick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CookieComponent cookieComponent;
    private final QUserRepository qUserRepository;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;

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

    public void changeUserRole(Long id, UserRoleType role) {
        UserEntity findUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
        findUser.changeUserRole(role);
    }

    public void changeProfileImage(String identifier, FileUploadRequest fileUploadRequest) {
        UserEntity findUser = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));

        String oldProfileImageUrl = findUser.getProfileImageUrl();

        findUser.changeProfileImage(fileUploadRequest.presignedUrl());
        temporalFileRepository.deleteById(fileUploadRequest.id());

        s3Service.deleteS3File(oldProfileImageUrl); // s3 파일 삭제는 항상 마지막에 호출
    }

    public UserResponseDto findMyInfo(String identifier) {
        UserEntity findUser = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));

        return new UserResponseDto(findUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
