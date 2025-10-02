package kakao.festapick.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewLowServiceTest {

    private final TestUtil testUtil = new TestUtil();

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewLowService reviewLowService;

    @Test
    @DisplayName("없는 리뷰 조회하는 경우 예외가 발생")
    void findReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewRepository.findByUserIdAndId(any(), any()))
                .willReturn(Optional.empty());

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> reviewLowService.findByUserIdAndId(review.getId(), user.getId()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_NOT_FOUND);

        verify(reviewRepository).findByUserIdAndId(any(), any());
        verifyNoMoreInteractions(reviewRepository);
    }

    private Festival testFestival() throws NoSuchFieldException, IllegalAccessException {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg", testUtil.toLocalDate("20250823"),
                testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, new TourDetailResponse());

        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);

        return festival;
    }



}