package kakao.festapick.wish.service;

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
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.dto.WishResponseDto;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WishServiceTest {

    @InjectMocks
    private WishService wishService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private FestivalLowService festivalLowService;

    @Mock
    private WishLowService wishLowService;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("위시 등록 성공")
    void createWishSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(festivalLowService.findFestivalById(any()))
                .willReturn(festival);
        given(userLowService.findById(any()))
                .willReturn(user);

        given(wishLowService.save(any())).willReturn(wish);

        WishResponseDto responseDto = wishService.createWish(festival.getId(),
                user.getId());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.wishId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalId())
                        .isEqualTo(festival.getId()),
                () -> AssertionsForClassTypes.assertThat(responseDto.title())
                        .isEqualTo(festival.getTitle()),
                () -> AssertionsForClassTypes.assertThat(responseDto.areaCode())
                        .isEqualTo(festival.getAreaCode())
        );

        verify(festivalLowService).findFestivalById(any());
        verify(userLowService).findById(any());
        verify(wishLowService).findByUserIdAndFestivalId(any(), any());
        verify(wishLowService).save(any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(wishLowService);
    }

    @Test
    @DisplayName("중복으로 인한 위시 등록 실패")
    void createWishFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(festivalLowService.findFestivalById(any()))
                .willReturn(festival);
        given(userLowService.findById(any()))
                .willReturn(user);
        given(wishLowService.findByUserIdAndFestivalId(any(), any()))
                .willReturn(Optional.of(wish));

        DuplicateEntityException e = Assertions.assertThrows(DuplicateEntityException.class,
                () -> wishService.createWish(festival.getId(), user.getId()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.WISH_DUPLICATE);

        verify(festivalLowService).findFestivalById(any());
        verify(userLowService).findById(any());
        verify(wishLowService).findByUserIdAndFestivalId(any(), any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(wishLowService);
    }

    @Test
    @DisplayName("위시 삭제 성공")
    void deleteWishSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(wishLowService.findByUserIdAndId(any(), any()))
                .willReturn(wish);

        wishService.removeWish(wish.getId(), user.getId());

        verify(wishLowService).findByUserIdAndId(any(), any());
        verify(wishLowService).delete(any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(wishLowService);
    }

    private Festival testFestival() throws NoSuchFieldException, IllegalAccessException {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg",
                testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, new TourDetailResponse());

        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);

        return festival;
    }

}
