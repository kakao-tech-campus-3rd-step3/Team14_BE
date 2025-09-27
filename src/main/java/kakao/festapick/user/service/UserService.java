package kakao.festapick.user.service;

import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.festival.service.FestivalService;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.review.service.ReviewService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.dto.UserResponseDtoForAdmin;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.user.repository.QUserRepository;
import kakao.festapick.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserLowService userLowService;
    private final FestivalService festivalService;
    private final WishRepository wishRepository;
    private final ReviewService reviewService;
    private final CookieComponent cookieComponent;
    private final QUserRepository qUserRepository;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;


    public void withDraw(Long userId, HttpServletResponse response) {

        UserEntity findUser = userLowService.findById(userId);

        deleteRelatedEntity(findUser);

        userLowService.deleteById(userId);
        response.setHeader("Set-Cookie", cookieComponent.deleteRefreshToken());

        s3Service.deleteS3File(findUser.getProfileImageUrl()); // s3 파일 삭제는 항상 마지막에 호출
    }

    public Page<UserResponseDtoForAdmin> findByIdentifierOrUserEmail(UserSearchCond userSearchCond, Pageable pageable) {
        return qUserRepository.findByIdentifierOrUserEmail(userSearchCond, pageable)
                .map(UserResponseDtoForAdmin::new);
    }

    public void changeUserRole(Long id, UserRoleType role) {
        UserEntity findUser = userLowService.findById(id);

        findUser.changeUserRole(role);
    }

    public void changeProfileImage(Long userId, FileUploadRequest fileUploadRequest) {
        UserEntity findUser = userLowService.findById(userId);

        String oldProfileImageUrl = findUser.getProfileImageUrl();

        findUser.changeProfileImage(fileUploadRequest.presignedUrl());
        temporalFileRepository.deleteById(fileUploadRequest.id());

        s3Service.deleteS3File(oldProfileImageUrl); // s3 파일 삭제는 항상 마지막에 호출
    }

    public UserResponseDto findMyInfo(Long userId) {
        UserEntity findUser = userLowService.findById(userId);

        return new UserResponseDto(findUser);
    }

    public void deleteUser(Long id) {
        UserEntity findUser = userLowService.findById(id);

        deleteRelatedEntity(findUser);

        userLowService.deleteById(findUser.getId());

        s3Service.deleteS3File(findUser.getProfileImageUrl()); // s3 파일 삭제는 항상 마지막에 호출
    }

    private void deleteRelatedEntity(UserEntity findUser) {
        wishRepository.deleteByUserId(findUser.getId());
        reviewService.deleteReviewByUserId(findUser.getId());
        festivalService.deleteFestivalByManagerId(findUser.getId());
    }


}
