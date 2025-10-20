package kakao.festapick.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.service.FestivalCacheService;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festival.tourapi.TourDetailResponse;
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
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private FestivalLowService festivalLowService;

    @Mock
    private ReviewLowService reviewLowService;

    @Mock
    private FileService fileService;

    @Mock
    private TemporalFileRepository temporalFileRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private FestivalCacheService festivalCacheService;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(festivalLowService.findFestivalById(any()))
                .willReturn(festival);
        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(reviewLowService.existsByUserIdAndFestivalId(any(), any()))
                .willReturn(false);
        given(reviewLowService.save(any()))
                .willReturn(review);

        ReviewRequestDto requestDto = new ReviewRequestDto(content, score, List.of(new FileUploadRequest(1L,"image")), null);

        Long savedId = reviewService.createReview(festival.getId(),
                requestDto, user.getId());

        assertThat(review.getId()).isEqualTo(savedId);

        verify(festivalLowService).findFestivalById(any());
        verify(userLowService).getReferenceById(any());
        verify(reviewLowService).existsByUserIdAndFestivalId(any(), any());
        verify(reviewLowService).save(any());
        verify(fileService).saveAll(anyList());
        verify(temporalFileRepository).deleteByIds(any());
        verifyNoMoreInteractions(festivalLowService,userLowService,reviewLowService,fileService, temporalFileRepository);
    }

    @Test
    @DisplayName("중복으로 인한 리뷰 등록 실패")
    void createReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;

        given(festivalLowService.findFestivalById(any()))
                .willReturn(festival);
        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(reviewLowService.existsByUserIdAndFestivalId(any(), any()))
                .willReturn(true);

        ReviewRequestDto requestDto = new ReviewRequestDto(content, score, null, null);

        DuplicateEntityException e = Assertions.assertThrows(DuplicateEntityException.class,
                () -> reviewService.createReview(festival.getId(), requestDto, user.getId()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_DUPLICATE);

        verify(festivalLowService).findFestivalById(any());
        verify(userLowService).getReferenceById(any());
        verify(reviewLowService).existsByUserIdAndFestivalId(any(), any());
        verifyNoMoreInteractions(festivalLowService,userLowService,reviewLowService,fileService, temporalFileRepository);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewLowService.findByUserIdAndId(any(), any()))
                .willReturn(review);

        reviewService.removeReview(review.getId(), user.getId());

        verify(reviewLowService).findByUserIdAndId(any(), any());
        verify(fileService).deleteByDomainId(any(),any());
        verify(reviewLowService).delete(any());
        verifyNoMoreInteractions(festivalLowService,userLowService,reviewLowService,fileService);
    }



    @Test
    @DisplayName("리뷰 삭제 실패 (없는 리뷰 삭제 시도)")
    void deleteReviewFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewLowService.findByUserIdAndId(any(), any()))
                .willThrow(new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> reviewService.removeReview(review.getId(), user.getId()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.REVIEW_NOT_FOUND);

        verify(reviewLowService).findByUserIdAndId(any(), any());
        verifyNoMoreInteractions(festivalLowService,userLowService,reviewLowService,fileService);
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReviewSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        String content = "test content";
        Integer score = 1;
        Review review = new Review(1L, user, festival, content, score);

        given(reviewLowService.findByUserIdAndId(any(), any()))
                .willReturn(review);

        given(fileService.findByDomainIdAndDomainType(any(), any()))
                .willReturn(List.of());

        ReviewRequestDto requestDto = new ReviewRequestDto("updated", 5, null, null);

        reviewService.updateReview(review.getId(), requestDto, user.getId());

        verify(reviewLowService).findByUserIdAndId(any(), any());
        verify(fileService).findByDomainIdAndDomainType(any(),any());
        verify(fileService).deleteAllByFileEntity(any());
        verify(temporalFileRepository).deleteByIds(any());
        verify(s3Service).deleteFiles(any());
        verifyNoMoreInteractions(festivalLowService,userLowService,reviewLowService,fileService, temporalFileRepository);
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

        given(reviewLowService.findById(any()))
                .willReturn(review);

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

    @Test
    @DisplayName("내가 리뷰한 축제 조회 성공 테스트")
    void getReviewedFestivalsSuccess() throws NoSuchFieldException, IllegalAccessException {

        // given
        UserEntity user = testUtil.createTestUser();
        List<Festival> festivalList = getTestFestivals();
        String content = "test content";
        Integer score = 1;
        List<Review> reviewList = new ArrayList<>();

        for (Festival festival: festivalList) {
            reviewList.add(new Review(1L, user, festival, content, score));
        }

        given(reviewLowService.findByUserIdWithAll(any(), any()))
                .willReturn(new PageImpl<>(reviewList));

        given(festivalCacheService.calculateReviewScore(any()))
                .willReturn(1.0);

        given(festivalCacheService.getWishCount(any()))
                .willReturn(0L);

        // when
        Page<FestivalListResponse> responseDto = reviewService.getMyReviewedFestivals(user.getId(),
                PageRequest.of(0, 3));

        // then
        for (int i = 0; i < 3; i++) {
            FestivalListResponse actucal = responseDto.getContent().get(i);
            Festival festival = festivalList.get(i);
            assertSoftly(softly-> {
                softly.assertThat(actucal.title()).isEqualTo(festival.getTitle());
                softly.assertThat(actucal.addr1()).isEqualTo(festival.getAddr1());
                softly.assertThat(actucal.addr2()).isEqualTo(festival.getAddr2());
                softly.assertThat(actucal.posterInfo()).isEqualTo(festival.getPosterInfo());
            });
        }

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

    private List<Festival> getTestFestivals() throws NoSuchFieldException, IllegalAccessException {
        List<Festival> festivalList = new ArrayList<>();
        festivalList.add(testFestival());
        festivalList.add(testFestival());
        festivalList.add(testFestival());
        return festivalList;
    }

}
