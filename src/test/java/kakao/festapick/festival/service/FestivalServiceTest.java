package kakao.festapick.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.dto.FestivalListResponseForAdmin;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.repository.QFestivalRepository;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.review.service.ReviewService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import kakao.festapick.wish.repository.WishRepository;
import org.aspectj.weaver.ast.Not;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private UserLowService userLowService;

    @Mock
    private QFestivalRepository qFestivalRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private TemporalFileRepository temporalFileRepository;

    @Mock
    private FileService fileService;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private FestivalService festivalService;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("사용자 축제 등록 성공")
    void addCustomizedFestival() throws Exception {

        //given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalCustomRequestDto requestDto = createCustomRequestDto();
        Festival festival = createCustomFestival(requestDto, user);
        Long festivalId = 1L;

        given(userLowService.findById(any())).willReturn(user);
        given(festivalRepository.save(any())).willReturn(festival);
        setFestivalId(festival, festivalId);

        //when
        Long Id = festivalService.addCustomizedFestival(requestDto, user.getId());

        //then
        assertAll(
                () -> assertThat(Id).isNotNull(),
                () -> assertThat(Id).isEqualTo(festival.getId())
        );

        verify(userLowService).findById(any());
        verify(festivalRepository).save(any());
        verify(temporalFileRepository).deleteById(any());
        verify(temporalFileRepository).deleteByIds(any());
        verifyNoMoreInteractions(userLowService, qFestivalRepository, s3Service, temporalFileRepository);
    }

    @Test
    @DisplayName("사용자 축제 등록 실패 - 존재하지 않는 사용자")
    void addCustomizedFestivalFail(){

        //given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalCustomRequestDto requestDto = createCustomRequestDto();

        given(userLowService.findById(any())).willThrow(new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));

        //when
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class, () -> festivalService.addCustomizedFestival(requestDto, user.getId())
        );

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);

        verify(userLowService).findById(any());
        verifyNoMoreInteractions(festivalRepository);
    }


    @Test
    @DisplayName("시작일이 종료일보다 늦을 경우 축제 예외 반환")
    void addCustomizedFestivalFail2() {
        // given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalCustomRequestDto requestDto =
                new FestivalCustomRequestDto(
                        "축제title", 32, "주소1", "상세주소",
                        new FileUploadRequest(1L,"imageUrl"), null, testUtil.toLocalDate("20250827"), testUtil.toLocalDate("20250825"),
                        "homepageUrl", "축제에 대한 개요");

        given(userLowService.findById(any()))
                .willReturn(user);

        // when & then
        assertThatThrownBy(()->
                festivalService.addCustomizedFestival(requestDto, user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ExceptionCode.FESTIVAL_BAD_DATE.getErrorMessage());

        verify(userLowService).findById(any());
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    @DisplayName("Id를 통해 축제의 정보를 검색(상세조회)")
    void findOneById() {

        //given
        Festival festival = createFestival();
        given(festivalRepository.findFestivalById(any())).willReturn(Optional.of(festival));

        //when
        FestivalDetailResponseDto response = festivalService.findOneById(festival.getId());

        //then
        assertAll(
                () -> assertThat(response.title()).isEqualTo(festival.getTitle()),
                () -> assertThat(response.contentId()).isEqualTo(festival.getContentId()),
                () -> assertThat(response.overView()).isEqualTo(festival.getOverView()),
                () -> assertThat(response.imageInfos()).isInstanceOf(List.class) //축제 관련 이미지가 List의 형태로 반환
        );

        verify(festivalRepository).findFestivalById(any());
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    @DisplayName("존재하지 않는 축제를 검색한 경우")
    void findOneByIdFail() {

        //given
        Festival festival = createFestival();
        given(festivalRepository.findFestivalById(any())).willReturn(Optional.empty());

        //when - then
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class, () -> festivalService.findOneById(festival.getId())
        );
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_NOT_FOUND);

        verify(festivalRepository).findFestivalById(any());
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    @DisplayName("지역 정보와 날짜를 통해 축제를 조회") //페이지네이션과 형 변환을 확인
    void findApprovedAreaAndDate() {

        //given
        int areaCode = 1;
        Pageable pageable = PageRequest.of(0, 2);
        List<Festival> festivals = getFestivals();
        Page<Festival> pagedFestivals = new PageImpl<>(festivals, pageable, 10);

        given(festivalRepository.findFestivalByAreaCodeAndDate(anyInt(), any(), any(), any())).willReturn(pagedFestivals);

        //when
        Page<FestivalListResponse> festivalList = festivalService.findApprovedAreaAndDate(areaCode, pageable);

        //then
        assertAll(
                () -> assertThat(festivalList.getContent().size()).isEqualTo(festivals.size()),
                () -> assertThat(festivalList.getPageable().getPageSize()).isEqualTo(pageable.getPageSize()),
                () -> assertThat(festivalList.getContent().getFirst()).isInstanceOf(FestivalListResponse.class)
        );

        verify(festivalRepository).findFestivalByAreaCodeAndDate(anyInt(), any(), any(), any());
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    @DisplayName("자신이 등록한 축제를 조회")
    void findMyFestivals(){

        //given
        String identifier = "KAKAO_987654";
        UserEntity user = testUtil.createTestManager(identifier);

        Festival customFestival1 = createCustomFestival(createCustomRequestDto(), user);
        Festival customFestival2 = createCustomFestival(createCustomRequestDto(), user);
        List<Festival> festivals = new ArrayList<>();
        festivals.add(customFestival1);
        festivals.add(customFestival2);

        Pageable pageable = PageRequest.of(0,2);
        Page<Festival> pagedFestivals = new PageImpl<>(festivals, pageable, 10);

        given(festivalRepository.findFestivalByManagerId(any(), any())).willReturn(pagedFestivals);

        //when
        Page<FestivalListResponse> result = festivalService.findMyFestivals(user.getId(), pageable);

        //total
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(10);

        verify(festivalRepository).findFestivalByManagerId(any(), any());
        verifyNoMoreInteractions(userLowService, festivalRepository);
    }

    @Test
    void findAllWithPage() {

        //given
        FestivalSearchCondForAdmin cond = new FestivalSearchCondForAdmin("title", null);
        Pageable pageable = PageRequest.of(0,2);
        List<Festival> festivals = getFestivals();
        Page<Festival> pagedFestivals = new PageImpl<>(festivals, pageable, 10);

        given(qFestivalRepository.findByStateAndTitleLike(any(), any())).willReturn(pagedFestivals);

        //when
        Page<FestivalListResponseForAdmin> result = festivalService.findAllWithPage(cond, pageable);

        //then(타입에 대한 검사)
        assertAll(
                () -> assertThat(result.getPageable().getPageSize()).isEqualTo(pageable.getPageSize()),
                () -> assertThat(result.getContent().size()).isEqualTo(festivals.size()),
                () -> assertThat(result.getContent().getFirst()).isInstanceOf(FestivalListResponseForAdmin.class)
        );
        verify(qFestivalRepository).findByStateAndTitleLike(any(), any());
        verifyNoMoreInteractions(qFestivalRepository);

    }

    @Test
    @DisplayName("자신이 등록한 축제를 변경 - 변경 성공(포스터 변경 x)")
    void updateFestival() {
        //given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalCustomRequestDto requestDto = createCustomRequestDto();
        Festival festival = createCustomFestival(requestDto, user);
        FestivalUpdateRequestDto updateInfo = createUpdateRequestDto();

        given(festivalRepository.findFestivalByIdWithManager(any())).willReturn(Optional.of(festival));

        //when
        FestivalDetailResponseDto updated = festivalService.updateFestival(user.getId(), any(), updateInfo);

        //then
        assertAll(
                () -> assertThat(updated.title()).isEqualTo(updateInfo.title()),
                () -> assertThat(updated.addr1()).isEqualTo(updateInfo.addr1())
        );
        verify(festivalRepository).findFestivalByIdWithManager(any());
        verify(temporalFileRepository).deleteByIds(anyList());
        verify(s3Service).deleteFiles(any());

        verifyNoMoreInteractions(festivalRepository, temporalFileRepository,s3Service);
    }

    @Test
    @DisplayName("자신이 등록하지 않은 축제를 수정 - 변경 실패")
    void updateFestivalFail() {
        //given
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = createFestival();

        FestivalUpdateRequestDto updateInfo = createUpdateRequestDto();

        given(festivalRepository.findFestivalByIdWithManager(any())).willReturn(Optional.of(festival));

        //when
        ForbiddenException e = assertThrows(
                ForbiddenException.class, () -> festivalService.updateFestival(user.getId(), any(), updateInfo)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_ACCESS_FORBIDDEN);
        verify(festivalRepository).findFestivalByIdWithManager(any());
        verifyNoMoreInteractions(festivalRepository, temporalFileRepository, s3Service);
    }

    @Test
    @DisplayName("등록되어 있지 않은 축제의 상태를 변경하려는 경우")
    void updateStateFestivalNotFound() {

        //given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalUpdateRequestDto updateInfo = createUpdateRequestDto();

        given(festivalRepository.findFestivalByIdWithManager(any())).willReturn(Optional.empty());

        //when
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class, () -> festivalService.updateFestival(user.getId(), any(), updateInfo)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_NOT_FOUND);
        verify(festivalRepository).findFestivalByIdWithManager(any());
        verifyNoMoreInteractions(festivalRepository);
    }

    @Test
    @DisplayName("자신이 등록한 축제를 삭제하는 경우")
    void removeOne() {

        //given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalCustomRequestDto requestDto = createCustomRequestDto();
        Festival festival = new Festival(requestDto, user);

        given(festivalRepository.findFestivalByIdWithManager(any())).willReturn(Optional.of(festival));

        //when
        festivalService.deleteFestivalForManager(user.getId(), any());

        //then
        verify(festivalRepository).findFestivalByIdWithManager(any());
        verify(festivalRepository).deleteById(any()); //행위 검증
        verify(fileService).deleteByDomainId(any(), any());
        verify(reviewService).deleteReviewByFestivalId(festival.getId());
        verify(wishRepository).deleteByFestivalId(festival.getId());
        verifyNoMoreInteractions(festivalRepository,fileService,reviewService,wishRepository);
    }

    @Test
    @DisplayName("자신이 등록하지 않은 축제를 삭제하려는 경우")
    void removeOneFail() {

        //given
        UserEntity user = testUtil.createTestUserWithId();
        FestivalCustomRequestDto requestDto = createCustomRequestDto();
        Festival festival = new Festival(requestDto, user);
        given(festivalRepository.findFestivalByIdWithManager(any())).willReturn(Optional.of(festival));

        //when
        ForbiddenException e = assertThrows(
                ForbiddenException.class, () -> festivalService.deleteFestivalForManager(100L, any())
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_ACCESS_FORBIDDEN);
        verify(festivalRepository).findFestivalByIdWithManager(any());
        verifyNoMoreInteractions(festivalRepository, wishRepository, reviewService);
    }

    @Test
    @DisplayName("관리자가 축제를 삭제하는 경우")
    void deleteFestivalForAdmin() throws Exception {
        //given
        Long festivalId = 1L;
        Festival festival = createFestival();
        setFestivalId(festival, festivalId);

        given(festivalRepository.findFestivalById(any())).willReturn(Optional.of(festival));

        //when
        festivalService.deleteFestivalForAdmin(festivalId);


        //then: 행위만을 검증
        verify(festivalRepository).deleteById(festivalId);
        verify(festivalRepository).findFestivalById(festivalId);
        verify(wishRepository).deleteByFestivalId(festivalId);
        verify(reviewService).deleteReviewByFestivalId(festivalId);
        verifyNoMoreInteractions(festivalRepository, wishRepository, reviewService);
    }

    private FestivalCustomRequestDto createCustomRequestDto() {
        return new FestivalCustomRequestDto(
                "축제title", 32, "주소1", "상세주소",
                new FileUploadRequest(1L,"imageUrl"), testUtil.createFestivalImages(), testUtil.toLocalDate("20250824"), testUtil.toLocalDate("20250825"),
                "homepageUrl", "축제에 대한 개요");

    }


    private FestivalRequestDto createRequestDto() {
        return new FestivalRequestDto(
                "contentId","축제title", 32, "주소1", "상세주소",
                "imageUrl", testUtil.toLocalDate("20250824"), testUtil.toLocalDate("20250825"));

    }

    private FestivalUpdateRequestDto createUpdateRequestDto() {
        List<FileUploadRequest> updatedImages = new ArrayList<>();
        updatedImages.add(new FileUploadRequest(99L, "https://www.festapick.updateimage.com"));
        updatedImages.add(new FileUploadRequest(99L, "https://www.festapick.updateimage2.com"));
        return new FestivalUpdateRequestDto("updated_title", 32, "updated_주소1", "상세주소",
                new FileUploadRequest(1L,"imageUrl"), updatedImages, testUtil.toLocalDate("20250824"), testUtil.toLocalDate("20250825"), "homepage", "overview");

    }

    private Festival createFestival() {
        return new Festival(createRequestDto(), new TourDetailResponse());
    }

    private Festival createCustomFestival(FestivalCustomRequestDto requestDto, UserEntity user){
        return new Festival(requestDto, user);
    }

    private List<Festival> getFestivals(){
        List<Festival> festivals = new ArrayList<>();
        festivals.add(createFestival());
        festivals.add(createFestival());
        festivals.add(createFestival());
        festivals.add(createCustomFestival(createCustomRequestDto(), testUtil.createTestUserWithId()));
        festivals.add(createCustomFestival(createCustomRequestDto(), testUtil.createTestUserWithId()));
        return festivals;
    }

    //reflection을 사용하여 id값 강제 설정
    private void setFestivalId(Festival festival, Long id) throws NoSuchFieldException, IllegalAccessException {
        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, id);
    }

}

