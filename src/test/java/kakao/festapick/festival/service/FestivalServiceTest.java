package kakao.festapick.festival.service;


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
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.repository.QFestivalRepository;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QFestivalRepository qFestivalRepository;

    @InjectMocks
    private FestivalService festivalService;

    @Test
    @DisplayName("사용자 축제 등록 성공")
    void addCustomizedFestival() throws NoSuchFieldException, IllegalAccessException {

        //given
        UserEntity user = createTestUser();
        CustomFestivalRequestDto requestDto = createCustomRequestDto();
        Festival festival = new Festival(requestDto, user);

        given(userRepository.findByIdentifier(any())).willReturn(Optional.of(user));
        given(festivalRepository.save(any())).willReturn(festival);
        setFestivalId(festival);

        //when
        Long Id = festivalService.addCustomizedFestival(requestDto, user.getIdentifier());

        //then
        assertAll(
                () -> assertThat(Id).isNotNull(),
                () -> assertThat(Id).isEqualTo(festival.getId())
        );

        verify(userRepository).findByIdentifier(any());
        verify(festivalRepository).save(any());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    @DisplayName("TourAPI로부터 가져온 축제 정보를 저장")
    void addFestival() throws NoSuchFieldException, IllegalAccessException {

        //given
        FestivalRequestDto requestDto = createRequestDto();
        TourDetailResponse tourDetailResponse = new TourDetailResponse();
        Festival festival = new Festival(requestDto, tourDetailResponse.getOverview(), tourDetailResponse.getOverview());

        given(festivalRepository.save(any())).willReturn(festival);
        setFestivalId(festival);

        //when
        Long Id = festivalService.addFestival(requestDto, tourDetailResponse);

        //then
        assertAll(
                () -> assertThat(Id).isNotNull(),
                () -> assertThat(Id).isEqualTo(festival.getId())
        );
        verify(festivalRepository).save(any());
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    void checkExistenceByContentId() {
    }

    @Test
    void findOneById() {
    }

    @Test
    void findApprovedOneById() {
    }

    @Test
    void findApprovedAreaAndDate() {
    }

    @Test
    void findApprovedOneByArea() {
    }

    @Test
    void findApprovedOneByKeyword() {
    }

    @Test
    void findApproved() {
    }

    @Test
    void findAllWithPage() {
    }

    @Test
    void updateFestival() {
    }

    @Test
    void updateState() {
    }

    @Test
    void removeOne() {
    }

    @Test
    void deleteFestivalForAdmin() {
    }

    private UserEntity createTestUser() {
        return new UserEntity(1L, "KAKAO-1234567890", "asd@test.com", "testUser", UserRoleType.USER, SocialType.KAKAO);
    }

    private CustomFestivalRequestDto createCustomRequestDto() {
        return new CustomFestivalRequestDto(
                "축제title", "32", "주소1", "상세주소",
                "imageUrl", toLocalDate("20250824"), toLocalDate("20250825"),
                "homepageUrl", "축제에 대한 개요");

    }

    private FestivalRequestDto createRequestDto() {
        return new FestivalRequestDto(
                "contentId","축제title", "32", "주소1", "상세주소",
                "imageUrl", toLocalDate("20250824"), toLocalDate("20250825"));

    }

    //reflection을 사용하여 id값 강제 설정
    private void setFestivalId(Festival festival) throws NoSuchFieldException, IllegalAccessException {
        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);
    }

    private LocalDate toLocalDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }




}

