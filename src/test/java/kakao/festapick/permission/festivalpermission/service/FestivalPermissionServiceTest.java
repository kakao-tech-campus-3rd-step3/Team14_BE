package kakao.festapick.permission.festivalpermission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.permission.PermissionFileUploader;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionDetailDto;
import kakao.festapick.permission.fmpermission.service.FMPermissionLowService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalPermissionServiceTest {

    @Mock
    private FestivalPermissionLowService festivalPermissionLowService;

    @Mock
    private FestivalLowService festivalLowService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private FileService fileService;

    @Mock
    private PermissionFileUploader permissionFileUploader;

    @InjectMocks
    private FestivalPermissionService festivalPermissionService;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("FestivalPermission 생성 실패 - 이미 관리자가 존재하는 경우")
    void createFestivalPermission() throws Exception {

        //given
        Festival festival = testUtil.createTourApiTestFestival();
        UserEntity manager = testUtil.createTestManager("KAKAO - 1234");
        festival.updateManager(manager);

        UserEntity user = testUtil.createTestUser();
        List<FileUploadRequest> uploadRequests = getFileUploadRequest();

        given(userLowService.getReferenceById(any())).willReturn(user);
        given(festivalLowService.findFestivalById(any())).willReturn(festival);

        //when
        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> festivalPermissionService.createFestivalPermission(user.getId(), festival.getId(), uploadRequests));

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_PERMISSION_BAD_REQUEST);
        verify(userLowService).getReferenceById(any());
        verify(festivalLowService).findFestivalById(any());
        verifyNoMoreInteractions(userLowService, festivalLowService);
    }

    @Test
    @DisplayName("FestivalPermission 생성 실패 - 동일 축제에 대해서 중복으로 신청 불가")
    void createFestivalPermissionFailDuplicate() {

        //given
        Festival festival = testUtil.createTourApiTestFestival();
        UserEntity user = testUtil.createTestUser();
        List<FileUploadRequest> uploadRequests = getFileUploadRequest();

        given(userLowService.getReferenceById(any())).willReturn(user);
        given(festivalLowService.findFestivalById(any())).willReturn(festival);
        given(festivalPermissionLowService.existsByUserIdAndFestivalId(any(), any())).willReturn(true);

        //when
        DuplicateEntityException e = assertThrows(
                DuplicateEntityException.class,
                () -> festivalPermissionService.createFestivalPermission(user.getId(), festival.getId(), uploadRequests));

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_PERMISSION_DUPLICATE);
        verify(userLowService).getReferenceById(any());
        verify(festivalLowService).findFestivalById(any());
        verify(festivalPermissionLowService).existsByUserIdAndFestivalId(any(), any());
        verifyNoMoreInteractions(userLowService, festivalLowService, festivalPermissionLowService);
    }

    @Test
    @DisplayName("ACCEPTED가 된 Permission에 대해서는 수정이 불가")
    void updateFestivalPermissionFail() throws Exception {

        //given
        UserEntity user = testUtil.createTestManager("KAKAO_102938");
        Festival festival = testUtil.createTourApiTestFestival();
        FestivalPermission festivalPermission = createFestivalPermission(user, festival, PermissionState.ACCEPTED);
        List<FileUploadRequest> uploadRequests = getFileUploadRequest();

        given(festivalPermissionLowService.findByIdAndUserId(any(), any())).willReturn(festivalPermission);

        //when
        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> festivalPermissionService.updateFestivalPermission(user.getId(), festivalPermission.getId(), uploadRequests)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.PERMISSION_ACCEPTED_BAD_REQUEST);
        verify(festivalPermissionLowService).findByIdAndUserId(any(), any());
        verifyNoMoreInteractions(festivalPermissionLowService);
    }

    @Test
    @DisplayName("수정이 된 permission은 다시 pending 상태가 된다")
    void updateFestivalPermission() throws Exception {

        //given
        UserEntity user = testUtil.createTestManager("KAKAO_102938");
        Festival festival = testUtil.createTourApiTestFestival();
        FestivalPermission festivalPermission = createFestivalPermission(user, festival, PermissionState.DENIED);
        List<FileUploadRequest> uploadRequests = getFileUploadRequest();
        List<FileEntity> fileEntities = getFileEntity(user.getId());

        given(festivalPermissionLowService.findByIdAndUserId(any(), any())).willReturn(festivalPermission);
        given(fileService.findByDomainIdAndDomainType(any(), any())).willReturn(fileEntities);

        //when
        assertThat(festivalPermission.getPermissionState()).isEqualTo(PermissionState.DENIED);
        FestivalPermissionDetailDto detailDto = festivalPermissionService.updateFestivalPermission(user.getId(), festivalPermission.getId(), uploadRequests);

        //then
        assertAll(
                () -> assertThat(detailDto.docs().size()).isEqualTo(fileEntities.size()),
                () -> assertThat(festivalPermission.getPermissionState()).isEqualTo(PermissionState.PENDING)
        );
        verify(permissionFileUploader).updateFiles(any(), any(), any());
        verifyNoMoreInteractions(festivalPermissionLowService);
    }

    @Test
    @DisplayName("축제 관리자로 승인 허용 - 축제의 외래키 설정")
    void updateFestivalPermissionState(){

        //given
        UserEntity user = testUtil.createTestManager("KAKAO-191736");
        Festival festival = testUtil.createTourApiTestFestival();
        FestivalPermission festivalPermission = new FestivalPermission(user, festival);
        given(festivalPermissionLowService.findByIdWithFestival(any())).willReturn(festivalPermission);

        //when
        assertThat(festival.getManager()).isNull();
        festivalPermissionService.updateFestivalPermissionState(festivalPermission.getId(), PermissionState.ACCEPTED);

        //then
        assertAll(
                () -> assertThat(festival.getManager()).isEqualTo(user),
                () -> assertThat(festivalPermission.getPermissionState()).isEqualTo(PermissionState.ACCEPTED)
        );
        verify(festivalPermissionLowService).findByIdWithFestival(any());
        verifyNoMoreInteractions(festivalPermissionLowService);

    }

    @Test
    @DisplayName("축제 관리자로 승인 - 이미 축제 관리자가 존재하는 경우에는 예외 발생")
    void updateFestivalPermissionStateDuplicateFail(){

        //given
        UserEntity user = testUtil.createTestManager("KAKAO-191736");
        Festival festival = testUtil.createTourApiTestFestival();

        UserEntity manager = testUtil.createTestManager("KAKAO-20251013");
        festival.updateManager(manager);

        FestivalPermission festivalPermission = new FestivalPermission(user, festival);
        given(festivalPermissionLowService.findByIdWithFestival(any())).willReturn(festivalPermission);

        //when
        assertThat(festival.getManager()).isEqualTo(manager);
        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> festivalPermissionService.updateFestivalPermissionState(festivalPermission.getId(), PermissionState.ACCEPTED)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_PERMISSION_BAD_REQUEST);
        verify(festivalPermissionLowService).findByIdWithFestival(any());
        verifyNoMoreInteractions(festivalPermissionLowService);
    }

    @Test
    @DisplayName("축제 관리자로 거절 - 축제 매니저는 유지 되어야함")
    void updateFestivalPermissionStateDeny(){

        //given
        UserEntity user = testUtil.createTestManager("KAKAO-191736");
        Festival festival = testUtil.createTourApiTestFestival();

        UserEntity manager = testUtil.createTestManager("KAKAO-20251013");
        festival.updateManager(manager);

        FestivalPermission festivalPermission = new FestivalPermission(user, festival);
        given(festivalPermissionLowService.findByIdWithFestival(any())).willReturn(festivalPermission);

        //when
        assertThat(festival.getManager()).isEqualTo(manager);
        festivalPermissionService.updateFestivalPermissionState(festivalPermission.getId(), PermissionState.DENIED);

        //then
        assertAll(
                ()-> assertThat(festival.getManager()).isEqualTo(manager),
                () -> assertThat(festivalPermission.getPermissionState()).isEqualTo(PermissionState.DENIED)
        );
        verify(festivalPermissionLowService).findByIdWithFestival(any());
        verifyNoMoreInteractions(festivalPermissionLowService);
    }

    @Test
    @DisplayName("Accepted Festival Permission 삭제 (관리 자격 박탈)")
    void removeFestivalPermission() throws Exception {

        //given
        UserEntity user = testUtil.createTestManager("KAKAO-191736");
        Festival festival = testUtil.createTourApiTestFestival();
        festival.updateManager(user);

        FestivalPermission festivalPermission = createFestivalPermission(user, festival, PermissionState.ACCEPTED);
        given(festivalPermissionLowService.findByIdAndUserIdWithFestival(any(), any())).willReturn(festivalPermission);

        //when
        assertThat(festival.getManager()).isEqualTo(user);
        festivalPermissionService.removeFestivalPermission(user.getId(), festivalPermission.getId());

        //then
        assertThat(festival.getManager()).isNull();
        verify(festivalPermissionLowService).removeById(any());
        verify(fileService).deleteByDomainId(any(), any());
        verifyNoMoreInteractions(festivalPermissionLowService, fileService);
    }

    private FestivalPermission createFestivalPermission(UserEntity user, Festival festival, PermissionState permissionState) throws Exception {
        FestivalPermission festivalPermission = new FestivalPermission(user, festival);
        Field stateField = FestivalPermission.class.getDeclaredField("permissionState");
        stateField.setAccessible(true);
        stateField.set(festivalPermission, permissionState);
        return festivalPermission;
    }

    private List<FileUploadRequest> getFileUploadRequest(){
        List<FileUploadRequest> list = new ArrayList<>();
        list.add(new FileUploadRequest(1L, "file1.com"));
        list.add(new FileUploadRequest(2L, "file2.com"));
        return list;
    }

    private List<FileEntity> getFileEntity(Long id){
        List<FileEntity> list = new ArrayList<>();
        list.add(new FileEntity("https://documents1.com", FileType.DOCUMENT, DomainType.FESTIVAL_PERMISSION, id));
        list.add(new FileEntity("https://documents1.com", FileType.DOCUMENT, DomainType.FESTIVAL_PERMISSION, id));
        return list;
    }
}