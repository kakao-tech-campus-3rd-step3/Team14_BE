package kakao.festapick.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
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
import org.assertj.core.api.AssertionsForClassTypes;
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
    private OAuth2UserService oAuth2UserService;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(festivalRepository.findFestivalById(any()))
                .willReturn(Optional.of(festival));
        given(oAuth2UserService.findByIdentifier(any()))
                .willReturn(user);
        given(reviewRepository.existsByUserIdAndFestivalId(any(), any()))
                .willReturn(false);
        given(reviewRepository.save(any()))
                .willReturn(review);

        ReviewRequestDto requestDto = new ReviewRequestDto(content, score);

        ReviewResponseDto responseDto = reviewService.createReview(festival.getId(),
                requestDto, user.getIdentifier());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.reviewId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(responseDto.reviewerName())
                        .isEqualTo(user.getUsername()),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalTitle())
                        .isEqualTo(festival.getTitle()),
                () -> AssertionsForClassTypes.assertThat(responseDto.content())
                        .isEqualTo(content),
                () -> AssertionsForClassTypes.assertThat(responseDto.score())
                        .isEqualTo(score)
        );

        verify(festivalRepository).findFestivalById(any());
        verify(oAuth2UserService).findByIdentifier(any());
        verify(reviewRepository).existsByUserIdAndFestivalId(any(), any());
        verify(reviewRepository).save(any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    @DisplayName("중복으로 인한 리뷰 등록 실패")
    void createReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;

        given(festivalRepository.findFestivalById(any()))
                .willReturn(Optional.of(festival));
        given(oAuth2UserService.findByIdentifier(any()))
                .willReturn(user);
        given(reviewRepository.existsByUserIdAndFestivalId(any(), any()))
                .willReturn(true);

        ReviewRequestDto requestDto = new ReviewRequestDto(content, score);

        DuplicateEntityException e = Assertions.assertThrows(DuplicateEntityException.class,
                () -> reviewService.createReview(festival.getId(), requestDto, user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_DUPLICATE);

        verify(festivalRepository).findFestivalById(any());
        verify(oAuth2UserService).findByIdentifier(any());
        verify(reviewRepository).existsByUserIdAndFestivalId(any(), any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.of(review));

        reviewService.removeReview(review.getId(), user.getIdentifier());

        verify(reviewRepository).findByUserIdentifierAndId(any(), any());
        verify(reviewRepository).delete(any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(reviewRepository);
    }



    @Test
    @DisplayName("리뷰 삭제 실패 (없는 리뷰 삭제 시도)")
    void deleteReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.empty());

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> reviewService.removeReview(review.getId(), user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_NOT_FOUND);

        verify(reviewRepository).findByUserIdentifierAndId(any(), any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.of(review));

        ReviewRequestDto requestDto = new ReviewRequestDto("updated", 5);

        ReviewResponseDto responseDto = reviewService.updateReview(review.getId(), requestDto, user.getIdentifier());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.reviewId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(responseDto.reviewerName())
                        .isEqualTo(user.getUsername()),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalTitle())
                        .isEqualTo(festival.getTitle()),
                () -> AssertionsForClassTypes.assertThat(responseDto.content())
                        .isEqualTo("updated"),
                () -> AssertionsForClassTypes.assertThat(responseDto.score())
                        .isEqualTo(5)
        );

        verify(reviewRepository).findByUserIdentifierAndId(any(), any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 수정 실패 (없는 리뷰 수정 시도)")
    void updateReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.empty());

        ReviewRequestDto requestDto = new ReviewRequestDto("updated", 5);

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> reviewService.updateReview(review.getId(), requestDto, user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_NOT_FOUND);

        verify(reviewRepository).findByUserIdentifierAndId(any(), any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(reviewRepository);
    }



    private Festival testFestival() throws NoSuchFieldException, IllegalAccessException {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                "11", "test area1", "test area2", "http://asd.example.com/test.jpg", "20250823",
                "20251231");
        Festival festival = new Festival(festivalRequestDto, "http://asd.example.com",
                "testtesttest");

        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);

        return festival;
    }

    private UserEntity testUser() {

        return new UserEntity(1L, "KAKAO-1234567890", "asd@test.com", "testUser", UserRoleType.USER,
                SocialType.KAKAO);
    }
}
