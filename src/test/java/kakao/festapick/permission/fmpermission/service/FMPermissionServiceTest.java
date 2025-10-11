package kakao.festapick.permission.fmpermission.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.permission.PermissionFileUploader;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.fmpermission.domain.FMPermission;
import kakao.festapick.permission.fmpermission.dto.FMPermissionAdminListResponseDto;
import kakao.festapick.permission.fmpermission.dto.FMPermissionResponseDto;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
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
class FMPermissionServiceTest {

    @Mock
    private FMPermissionLowService fmPermissionLowService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private FileService fileService;

    @Mock
    private TemporalFileRepository temporalFileRepository;

    @Mock
    private PermissionFileUploader permissionFileUploader;

    @InjectMocks
    private FMPermissionService fmPermissionService;

    private TestUtil testUtil = new TestUtil();

    private FMPermission newFMPermission(UserEntity user) throws Exception {
        FMPermission fmPermission = new FMPermission(user, "부산대학교");
        Field idField = FMPermission.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(fmPermission, 1L);
        return fmPermission;
    }

    private List<FileUploadRequest> getFileUploadRequest(){
        List<FileUploadRequest> list = new ArrayList<>();
        list.add(new FileUploadRequest(1L, "file1.com"));
        list.add(new FileUploadRequest(2L, "file2.com"));
        return list;
    }

    private List<FileEntity> getDocs(Long id){
        List<FileEntity> list = new ArrayList<>();
        list.add(new FileEntity(1L, "file1.com", FileType.DOCUMENT, DomainType.FM_PERMISSION, id));
        list.add(new FileEntity(2L, "file2.com", FileType.DOCUMENT, DomainType.FM_PERMISSION, id));
        return list;
    }

    private List<FileUploadRequest> updatedDocs(){
        List<FileUploadRequest> list = new ArrayList<>();
        list.add(new FileUploadRequest(1L, "file1.com"));
        list.add(new FileUploadRequest(2L, "file3.com"));
        return list;
    }

    @Test
    @DisplayName("FMPermission을 생성")
    void createFMPermission() throws Exception {

        //given
        UserEntity user = testUtil.createTestUser();
        FMPermission fmPermission = newFMPermission(user);

        List<FileUploadRequest> documents = getFileUploadRequest();
        String department = "부산대학교";

        given(userLowService.findById(any())).willReturn(user);
        given(fmPermissionLowService.existsByUserId(any())).willReturn(false);
        given(fmPermissionLowService.saveFMPermission(any())).willReturn(fmPermission);

        //when
        Long id = fmPermissionService.createFMPermission(user.getId(), documents, department);

        //then
        assertThat(id).isEqualTo(fmPermission.getId());
        verify(userLowService).findById(any());
        verify(fmPermissionLowService).existsByUserId(any());
        verify(fmPermissionLowService).saveFMPermission(any());
        verify(permissionFileUploader).saveFiles(any(), any(),any());

        verifyNoMoreInteractions(userLowService, fmPermissionLowService, permissionFileUploader);
    }

    @Test
    @DisplayName("중복해서 신청한 경우")
    void createFMPermissionFail(){

        //given
        UserEntity user = testUtil.createTestUser();

        List<FileUploadRequest> documents = getFileUploadRequest();
        String department = "부산대학교";

        given(userLowService.findById(any())).willReturn(user);
        given(fmPermissionLowService.existsByUserId(any())).willReturn(true);

        //when - then
        DuplicateEntityException e = assertThrows(
                DuplicateEntityException.class,
                () -> fmPermissionService.createFMPermission(user.getId(), documents, department)
        );
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FM_PERMISSION_DUPLICATE);
        verify(userLowService).findById(any());
        verify(fmPermissionLowService).existsByUserId(any());
        verifyNoMoreInteractions(userLowService, fmPermissionLowService);
    }

    @Test
    @DisplayName("서류를 업데이트")
    void updateDocuments() throws Exception {

        // given
        UserEntity user = testUtil.createTestUser();
        FMPermission fmPermission = newFMPermission(user);

        List<FileUploadRequest> updateRequest = updatedDocs();
        List<FileEntity> updatedDocs = updateRequest.stream()
                .map(req -> new FileEntity(req.presignedUrl(), FileType.DOCUMENT, DomainType.FM_PERMISSION, fmPermission.getId()))
                .toList();

        given(fmPermissionLowService.findFMPermissionByUserId(any())).willReturn(fmPermission);
        given(fileService.findByDomainIdAndDomainType(any(), any()))
                .willReturn(updatedDocs);

        //when
        FMPermissionResponseDto responseDto = fmPermissionService.updateDocuments(user.getId(), updateRequest);

        //then
        assertAll(
                () -> assertThat(fmPermission.getPermissionState()).isEqualTo(PermissionState.PENDING),
                () -> assertThat(responseDto.state()).isEqualTo(PermissionState.PENDING),
                () -> assertThat(responseDto.docsUrls()).isEqualTo(updatedDocs.stream().map(FileEntity::getUrl).toList())
        );

        verify(fmPermissionLowService).findFMPermissionByUserId(any());
        verify(permissionFileUploader).updateFiles(any(), any(), any());
        verify(fileService).findByDomainIdAndDomainType(any(), any());
        verifyNoMoreInteractions(fmPermissionLowService, permissionFileUploader, fileService);
    }

    @Test
    @DisplayName("UserId를 통한 FMPermission 조회")
    void getFMPermissionByUserId() throws Exception {

        //given
        UserEntity user = testUtil.createTestUser();
        FMPermission fmPermission = newFMPermission(user);
        List<FileEntity> docs = getDocs(fmPermission.getId());

        given(fmPermissionLowService.findFMPermissionByUserId(any())).willReturn(fmPermission);
        given(fileService.findByDomainIdAndDomainType(any(), any())).willReturn(docs);

        //when
        FMPermissionResponseDto responseDto = fmPermissionService.getFMPermissionByUserId(user.getId());

        //then
        assertAll(
                () -> assertThat(responseDto.docsUrls().size()).isEqualTo(docs.size()),
                () -> assertThat(responseDto.id()).isEqualTo(fmPermission.getId())
        );

        verify(fmPermissionLowService).findFMPermissionByUserId(any());
        verify(fileService).findByDomainIdAndDomainType(any(), any());
        verifyNoMoreInteractions(fmPermissionLowService, fileService);

    }


    @Test
    @DisplayName("UserId를 통한 FMPermission 삭제")
    void removeMyFMPermission() throws Exception {

        //given
        UserEntity user = testUtil.createTestUser();
        FMPermission fmPermission = newFMPermission(user);

        given(fmPermissionLowService.findFMPermissionByUserId(any())).willReturn(fmPermission);

        //when
        fmPermissionService.removeFMPermission(user.getId());

        //then
        verify(fmPermissionLowService).removeFMPermissionByUserId(any());
        verify(fileService).deleteByDomainId(any(), any());
        verifyNoMoreInteractions(fmPermissionLowService, fileService);
    }

    @Test
    @DisplayName("모든 FMPermission을 가져오기 - 관리자 권한")
    void getAllFMPermission() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 5);

        List<FMPermission> fmPermissionList = new ArrayList<>();
        UserEntity user1 = testUtil.createTestUser("KAKAO-1234");
        FMPermission fmPermission1 = newFMPermission(user1);
        fmPermissionList.add(fmPermission1);

        UserEntity user2 = testUtil.createTestUser("GOOGLE-1234");
        FMPermission fmPermission2 = newFMPermission(user2);
        fmPermissionList.add(fmPermission2);

        Page<FMPermission> contentPage = new PageImpl<>(fmPermissionList);

        given(fmPermissionLowService.findAll(any())).willReturn(contentPage);

        //when
        Page<FMPermissionAdminListResponseDto> responseDtoPage = fmPermissionService.getAllFMPermission(pageable);

        //then
        assertAll(
                () -> assertThat(responseDtoPage.getTotalElements()).isEqualTo(fmPermissionList.size()),
                () -> assertThat(responseDtoPage.getContent().getFirst()).isInstanceOf(FMPermissionAdminListResponseDto.class)
        );
        verify(fmPermissionLowService).findAll(any());
        verifyNoMoreInteractions(fmPermissionLowService);

    }

    @Test
    @DisplayName("요청의 상태 변경(요청 승인) - 관리자 권한")
    void updateStateAccepted() throws Exception {
        //given
        UserEntity user = testUtil.createTestUser();
        FMPermission fmPermission = newFMPermission(user);
        PermissionState state = PermissionState.ACCEPTED;
        given(fmPermissionLowService.findFMPermissionById(any())).willReturn(fmPermission);

        assertThat(user.getRoleType()).isEqualTo(UserRoleType.USER);

        //when
        fmPermissionService.updateState(fmPermission.getId(), state);

        //then
        assertAll(
                () -> assertThat(user.getRoleType()).isEqualTo(UserRoleType.FESTIVAL_MANAGER),
                () -> assertThat(fmPermission.getPermissionState()).isEqualTo(state)
        );

        verify(fmPermissionLowService).findFMPermissionById(any());
        verifyNoMoreInteractions(fmPermissionLowService);
    }

    @Test
    @DisplayName("요청의 상태 변경(요청 거절) - 관리자 권한")
    void updateStateDenied() throws Exception {
        //given
        UserEntity user = testUtil.createTestUser();
        FMPermission fmPermission = newFMPermission(user);
        PermissionState state = PermissionState.DENIED;
        given(fmPermissionLowService.findFMPermissionById(any())).willReturn(fmPermission);

        assertThat(user.getRoleType()).isEqualTo(UserRoleType.USER);

        //when
        fmPermissionService.updateState(fmPermission.getId(), state);

        //then
        assertAll(
                () -> assertThat(user.getRoleType()).isEqualTo(UserRoleType.USER),
                () -> assertThat(fmPermission.getPermissionState()).isEqualTo(state)
        );

        verify(fmPermissionLowService).findFMPermissionById(any());
        verifyNoMoreInteractions(fmPermissionLowService);
    }
}