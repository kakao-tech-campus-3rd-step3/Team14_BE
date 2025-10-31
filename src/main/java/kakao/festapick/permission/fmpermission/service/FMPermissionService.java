package kakao.festapick.permission.fmpermission.service;

import java.util.List;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festivalnotice.Repository.FestivalNoticeRepository;
import kakao.festapick.festivalnotice.service.FestivalNoticeService;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.FileUploadHelper;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.service.FestivalPermissionLowService;
import kakao.festapick.permission.fmpermission.domain.FMPermission;
import kakao.festapick.permission.fmpermission.dto.FMPermissionAdminListResponseDto;
import kakao.festapick.permission.fmpermission.dto.FMPermissionResponseDto;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FMPermissionService {

    private final FMPermissionLowService fmPermissionLowService;
    private final UserLowService userLowService;
    private final FestivalPermissionLowService festivalPermissionLowService;
    private final FestivalNoticeService festivalNoticeService;
    private final FestivalLowService festivalLowService;


    private final FileUploadHelper fileUploadHelper;
    private final FileService fileService;

    //신청
    public Long createFMPermission(
            Long userId,
            List<FileUploadRequest> documents,
            String department
    ){

        //role 검사
        UserEntity user = userLowService.findById(userId);
        if(user.getRoleType().equals(UserRoleType.FESTIVAL_MANAGER)){
            throw new BadRequestException(ExceptionCode.FM_PERMISSION_BAD_REQUEST);
        }

        //중복 검사
        if(fmPermissionLowService.existsByUserId(userId)){
            throw new DuplicateEntityException(ExceptionCode.FM_PERMISSION_DUPLICATE);
        }

        //Entity를 생성하고
        FMPermission fmPermission = new FMPermission(user, department);
        Long fmPermissionId = fmPermissionLowService.saveFMPermission(fmPermission).getId();

        //관련 서류 저장
        fileUploadHelper.saveFiles(documents, fmPermissionId, FileType.DOCUMENT, DomainType.FM_PERMISSION);
        return fmPermissionId;
    }

    // 업데이트(반려 또는 대기 중인 경우 서류를 수정하도록)
    public FMPermissionResponseDto updateDocuments(Long userId, List<FileUploadRequest> documents){

        FMPermission fmPermission = fmPermissionLowService.findFMPermissionByUserId(userId);
        Long permissionId = fmPermission.getId();

        if(fmPermission.getPermissionState().equals(PermissionState.ACCEPTED)){
            throw new BadRequestException(ExceptionCode.FM_PERMISSION_BAD_REQUEST);
        }

        fileUploadHelper.updateFiles(permissionId, DomainType.FM_PERMISSION, FileType.DOCUMENT, documents);

        fmPermission.updateState(PermissionState.PENDING);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(permissionId, DomainType.FM_PERMISSION)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();

        return new FMPermissionResponseDto(fmPermission, docsUrl);
    }

    //중복 조회
    @Transactional(readOnly = true)
    public Boolean checkFMPermission(Long userId){
        return fmPermissionLowService.existsByUserId(userId);
    }

    //조회
    @Transactional(readOnly = true)
    public FMPermissionResponseDto getFMPermissionByUserId(Long userId){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionByUserId(userId);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(fmPermission.getId(), DomainType.FM_PERMISSION)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();
        return new FMPermissionResponseDto(fmPermission, docsUrl);
    }

    @Transactional(readOnly = true)
    public FMPermissionResponseDto getFMPermissionById(Long id){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionById(id);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(fmPermission.getId(), DomainType.FM_PERMISSION)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();
        return new FMPermissionResponseDto(fmPermission, docsUrl);
    }

    //삭제
    public void removeFMPermission(Long userId){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionByUserId(userId);
        if(fmPermission.getPermissionState().equals(PermissionState.ACCEPTED)){
            fmPermission.getUser().changeUserRole(UserRoleType.USER);

            removeManagerFromFestival(userId); // 관리하고 있던 축제의 매니저를 null로 설정
            removeRelatedEntity(userId); // festival permission과 festival notice를 삭제
        }
        fmPermissionLowService.removeFMPermissionByUserId(userId);
        fileService.deleteByDomainId(fmPermission.getId(), DomainType.FM_PERMISSION); //첨부 했던 모든 문서들 삭제
    }

    private void removeManagerFromFestival(Long userId){
        festivalPermissionLowService.findByUserIdWithFestival(userId)
                .forEach(fm -> fm.getFestival().updateManager(null));
    }

    //user탈퇴 시, 관련 정보를 모두 삭제
    public void deleteFMPermissionByUserId(Long userId){
        fmPermissionLowService.getOptionalFMPermissionByUserId(userId)
                .ifPresent(fmPermission -> fileService.deleteByDomainId(fmPermission.getId(), DomainType.FM_PERMISSION));
        fmPermissionLowService.removeFMPermissionByUserId(userId);
    }

    //admin - 전체 조회
    @Transactional(readOnly = true)
    public Page<FMPermissionAdminListResponseDto> getAllFMPermission(Pageable pageable){
        return fmPermissionLowService.findAll(pageable).map(FMPermissionAdminListResponseDto::new);
    }

    //admin - 승인
    public void updateState(Long id, PermissionState permissionState){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionByIdWithUser(id);
        fmPermission.updateState(permissionState);

        UserEntity user = fmPermission.getUser();
        if(permissionState.equals(PermissionState.ACCEPTED)){
            user.changeUserRole(UserRoleType.FESTIVAL_MANAGER); //UserRole 변경
            return;
        }
        user.changeUserRole(UserRoleType.USER);
        removeManagerFromFestival(user.getId());
        removeRelatedEntity(user.getId());
    }

    private void removeRelatedEntity(Long userId){

        // 내가 등록한 축제 모두 삭제 (custom 축제)
        festivalLowService.deleteCustomFestivalByUserId(userId);

        // 연관된 Festival Permission 모두 삭제
        festivalPermissionLowService.deleteByUserId(userId);

        // 작성했던 모든 축제 공지 삭제
        festivalNoticeService.deleteByUserId(userId);
    }

}
