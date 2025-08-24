package kakao.festapick.wish.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.service.OAuth2UserService;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.repository.WishRepository;
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
    private OAuth2UserService oAuth2UserService;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private WishRepository wishRepository;

    @Test
    @DisplayName("위시 등록 성공")
    void createWishSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(festivalRepository.findFestivalById(any()))
                .willReturn(Optional.of(festival));
        given(oAuth2UserService.findByIdentifier(any()))
                .willReturn(user);
        given(wishRepository.findByUserIdentifierAndFestivalId(any(), any()))
                .willReturn(Optional.empty());
        given(wishRepository.save(any()))
                .willReturn(wish);

        WishResponseDto responseDto = wishService.createWish(festival.getId(),
                user.getIdentifier());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.wishId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalId())
                        .isEqualTo(festival.getId()),
                () -> AssertionsForClassTypes.assertThat(responseDto.title())
                        .isEqualTo(festival.getTitle()),
                () -> AssertionsForClassTypes.assertThat(responseDto.areaCode())
                        .isEqualTo(festival.getAreaCode())
        );

        verify(festivalRepository).findFestivalById(any());
        verify(oAuth2UserService).findByIdentifier(any());
        verify(wishRepository).findByUserIdentifierAndFestivalId(any(), any());
        verify(wishRepository).save(any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(wishRepository);
    }

    @Test
    @DisplayName("중복으로 인한 위시 등록 실패")
    void createWishFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(festivalRepository.findFestivalById(any()))
                .willReturn(Optional.of(festival));
        given(oAuth2UserService.findByIdentifier(any()))
                .willReturn(user);
        given(wishRepository.findByUserIdentifierAndFestivalId(any(), any()))
                .willReturn(Optional.of(wish));

        DuplicateEntityException e = Assertions.assertThrows(DuplicateEntityException.class,
                () -> wishService.createWish(festival.getId(), user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.WISH_DUPLICATE);

        verify(festivalRepository).findFestivalById(any());
        verify(oAuth2UserService).findByIdentifier(any());
        verify(wishRepository).findByUserIdentifierAndFestivalId(any(), any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(wishRepository);
    }

    @Test
    @DisplayName("위시 삭제 성공")
    void deleteWishSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(wishRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.of(wish));

        wishService.removeWish(wish.getId(), user.getIdentifier());

        verify(wishRepository).findByUserIdentifierAndId(any(), any());
        verify(wishRepository).delete(any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(wishRepository);
    }

    @Test
    @DisplayName("위시 삭제 실패 (없는 위시 삭제 시도)")
    void deleteWishFail() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUser();
        Festival festival = testFestival();
        Wish wish = new Wish(1L, user, festival);

        given(wishRepository.findByUserIdentifierAndId(any(), any()))
                .willReturn(Optional.empty());

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> wishService.removeWish(wish.getId(), user.getIdentifier()));
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.WISH_NOT_FOUND);

        verify(wishRepository).findByUserIdentifierAndId(any(), any());
        verifyNoMoreInteractions(festivalRepository);
        verifyNoMoreInteractions(oAuth2UserService);
        verifyNoMoreInteractions(wishRepository);
    }

    private Festival testFestival() throws NoSuchFieldException, IllegalAccessException {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                "11", "test area1", "test area2", "http://asd.example.com/test.jpg",
                toLocalDate("20250823"), toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, "http://asd.example.com",
                "testtesttest");

        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);

        return festival;
    }

    private UserEntity testUser() {
        return new UserEntity(1L, "KAKAO-1234567890", "asd@test.com", "testUser", UserRoleType.USER, SocialType.KAKAO);
    }

    private LocalDate toLocalDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

}
