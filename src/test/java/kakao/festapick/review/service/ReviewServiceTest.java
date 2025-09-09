package kakao.festapick.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.service.OAuth2UserService;
import kakao.festapick.user.service.UserService;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private FileService fileService;

    @Mock
    private TemporalFileRepository temporalFileRepository;

    @Mock
    private S3Service s3Service;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(festivalRepository.findFestivalById(any()))
                .willReturn(Optional.of(festival));
        given(userService.findByIdentifier(any()))
                .willReturn(user);
        given(reviewRepository.existsByUserIdAndFestivalId(any(), any()))
                .willReturn(false);
        given(reviewRepository.save(any()))
                .willReturn(review);

        ReviewRequestDto requestDto = new ReviewRequestDto(content, score, List.of(new FileUploadRequest(1L,"image")), null);

        Long savedId = reviewService.createReview(festival.getId(),
                requestDto, user.getIdentifier());

        assertThat(review.getId()).isEqualTo(savedId);

        verify(festivalRepository).findFestivalById(any());
        verify(userService).findByIdentifier(any());
        verify(reviewRepository).existsByUserIdAndFestivalId(any(), any());
        verify(reviewRepository).save(any());
        verify(fileService).saveAll(anyList());
        verify(temporalFileRepository).deleteByIds(any());
        verifyNoMoreInteractions(festivalRepository,userService,reviewRepository,fileService, temporalFileRepository);
    }

    @Test
    @DisplayName("중복으로 인한 리뷰 등록 실패")
    void createReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;

        given(festivalRepository.findFestivalById(any()))
                .willReturn(Optional.of(festival));
        given(userService.findByIdentifier(any()))
                .willReturn(user);
        given(reviewRepository.existsByUserIdAndFestivalId(any(), any()))
                .willReturn(true);

        ReviewRequestDto requestDto = new ReviewRequestDto(content, score, null, null);

        DuplicateEntityException e = Assertions.assertThrows(DuplicateEntityException.class,
                () -> reviewService.createReview(festival.getId(), requestDto, user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_DUPLICATE);

        verify(festivalRepository).findFestivalById(any());
        verify(userService).findByIdentifier(any());
        verify(reviewRepository).existsByUserIdAndFestivalId(any(), any());
        verifyNoMoreInteractions(festivalRepository,userService,reviewRepository,fileService, temporalFileRepository);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.deleteByUserIdentifierAndId(any(), any()))
                .willReturn(1);

        reviewService.removeReview(review.getId(), user.getIdentifier());

        verify(reviewRepository).deleteByUserIdentifierAndId(any(), any());
        verify(fileService).deleteByDomainId(any(),any());
        verifyNoMoreInteractions(festivalRepository,userService,reviewRepository,fileService);
    }



    @Test
    @DisplayName("리뷰 삭제 실패 (없는 리뷰 삭제 시도)")
    void deleteReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.deleteByUserIdentifierAndId(any(), any()))
                .willReturn(0);

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> reviewService.removeReview(review.getId(), user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_NOT_FOUND);

        verify(reviewRepository).deleteByUserIdentifierAndId(any(), any());
        verifyNoMoreInteractions(festivalRepository,userService,reviewRepository,fileService);
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.of(review));

        given(fileService.findByDomainIdAndDomainType(any(), any()))
                .willReturn(List.of());

        ReviewRequestDto requestDto = new ReviewRequestDto("updated", 5, null, null);

        reviewService.updateReview(review.getId(), requestDto, user.getIdentifier());

        verify(reviewRepository).findByUserIdentifierAndId(any(), any());
        verify(fileService).findByDomainIdAndDomainType(any(),any());
        verify(fileService).deleteAllByFileEntity(any());
        verify(temporalFileRepository).deleteByIds(any());
        verify(s3Service).deleteFiles(any());
        verifyNoMoreInteractions(festivalRepository,userService,reviewRepository,fileService, temporalFileRepository);
    }

    @Test
    @DisplayName("리뷰 수정 실패 (없는 리뷰 수정 시도)")
    void updateReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.empty());

        ReviewRequestDto requestDto = new ReviewRequestDto("updated", 5, null, null);

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> reviewService.updateReview(review.getId(), requestDto, user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_NOT_FOUND);

        verify(reviewRepository).findByUserIdentifierAndId(any(), any());
        verifyNoMoreInteractions(festivalRepository,userService,reviewRepository,fileService, temporalFileRepository);
    }

    @Test
    @DisplayName("리뷰 id로 조회")
    void getReviewByIdSuccess() throws NoSuchFieldException, IllegalAccessException {

        // given

        UserEntity user = testUtil.createTestUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findById(any()))
                .willReturn(Optional.of(review));

        // when
        ReviewResponseDto reviewDto = reviewService.getReview(review.getId());

        // then
        assertSoftly(softly-> {
            softly.assertThat(reviewDto.reviewId()).isEqualTo(review.getId());
            softly.assertThat(reviewDto.reviewerName()).isEqualTo(review.getReviewerName());
            softly.assertThat(reviewDto.content()).isEqualTo(content);
            softly.assertThat(reviewDto.festivalTitle()).isEqualTo(festival.getTitle());
            softly.assertThat(reviewDto.score()).isEqualTo(score);
            softly.assertThat(reviewDto.imageUrls()).isNull();
            softly.assertThat(reviewDto.videoUrl()).isNull();
        });

    }



    private Festival testFestival() throws NoSuchFieldException, IllegalAccessException {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg", testUtil.toLocalDate("20250823"),
                testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, "http://asd.example.com",
                "testtesttest");

        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);

        return festival;
    }

}
