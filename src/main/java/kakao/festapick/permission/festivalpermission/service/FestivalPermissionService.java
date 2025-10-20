package kakao.festapick.permission.festivalpermission.service;

import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festivalnotice.service.FestivalNoticeService;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.FileUploadHelper;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionAdminListDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionDetailDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionResponseListDto;
import kakao.festapick.permission.fmpermission.service.FMPermissionLowService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FestivalPermissionService {

    private final FestivalPermissionLowService festivalPermissionLowService;
    private final FMPermissionLowService fmPermissionLowService;
    private final FestivalLowService festivalLowService;
    private final UserLowService userLowService;

    private final FileService fileService;
    private final FileUploadHelper fileUploadHelper;
    private final FestivalNoticeService festivalNoticeService;

    public Long createFestivalPermission(Long userId, Long festivalId, List<FileUploadRequest> documents){
        UserEntity user = userLowService.getReferenceById(userId);
        Festival festival = festivalLowService.findFestivalById(festivalId);

        if(festival.getManager() != null){
            throw new BadRequestException(ExceptionCode.FESTIVAL_PERMISSION_BAD_REQUEST);
        }

        if(festivalPermissionLowService.existsByUserIdAndFestivalId(userId, festivalId)){
            throw new DuplicateEntityException(ExceptionCode.FESTIVAL_PERMISSION_DUPLICATE);
        }

        FestivalPermission festivalPermission = new FestivalPermission(user, festival);
        Long savedId = festivalPermissionLowService.save(festivalPermission).getId();

        fileUploadHelper.saveFiles(documents, savedId, FileType.IMAGE, DomainType.FESTIVAL_PERMISSION);
        return savedId;
    }

    @Transactional(readOnly = true)
    public Boolean checkFestivalPermission(Long userId, Long festivalId){
        return festivalPermissionLowService.existsByUserIdAndFestivalId(userId, festivalId);
    }

    @Transactional(readOnly = true)
    public Page<FestivalPermissionResponseListDto> getMyFestivalPermissionsByUserId(Long userId, Pageable pageable){
        return festivalPermissionLowService.findFestivalPermissionsByUserIdWithFestival(userId, pageable)
                .map(FestivalPermissionResponseListDto::new);
    }

    @Transactional(readOnly = true)
    public FestivalPermissionDetailDto getFestivalPermissionByUserId(Long userId, Long id){
        FestivalPermission permission = festivalPermissionLowService.findByIdAndUserIdWithFestival(id, userId);
        List<String> docsUrl = getDocumentsUrlById(id);
        return new FestivalPermissionDetailDto(permission, docsUrl);
    }

    public FestivalPermissionDetailDto updateFestivalPermission(Long userId, Long id, List<FileUploadRequest> documents){
        FestivalPermission festivalPermission = festivalPermissionLowService.findByIdAndUserId(id, userId);

        if(festivalPermission.getPermissionState().equals(PermissionState.ACCEPTED)){
            throw new BadRequestException(ExceptionCode.PERMISSION_ACCEPTED_BAD_REQUEST);
        }

        fileUploadHelper.updateFiles(id, DomainType.FESTIVAL_PERMISSION, FileType.IMAGE, documents);

        festivalPermission.updateState(PermissionState.PENDING);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(id, DomainType.FESTIVAL_PERMISSION)
                .stream()
                .map(FileEntity::getUrl)
                .toList();

        return new FestivalPermissionDetailDto(festivalPermission, docsUrl);
    }

    //신청서 취소
    public void removeFestivalPermission(Long userId, Long id){
        FestivalPermission festivalPermission = festivalPermissionLowService.findByIdAndUserIdWithFestival(id, userId);

        if(festivalPermission.getPermissionState().equals(PermissionState.ACCEPTED)){
            Festival festival = festivalPermission.getFestival();
            festival.updateManager(null);

            // 작성했던 모든 축제 공지 삭제
            festivalNoticeService.deleteByFestivalId(festival.getId());
        }

        festivalPermissionLowService.removeById(id);
        fileService.deleteByDomainId(festivalPermission.getId(), DomainType.FESTIVAL_PERMISSION);//첨부 했던 모든 문서들 삭제
    }

    //user탈퇴 시, 모든 내역을 삭제
    public void deleteFestivalPermissionByUserId(Long userId){
        List<Long> festivalPermissionIds = festivalPermissionLowService.findByUserId(userId)
                .stream()
                .map(permission -> permission.getId())
                .toList();
        festivalPermissionLowService.deleteByUserId(userId);
        fileService.deleteByDomainIds(festivalPermissionIds, DomainType.FESTIVAL_PERMISSION);
    }

    //축제 삭제 시, 관련된 요청을 모두 삭제
    public void deleteFestivalPermissionByFestivalId(Long festivalId){
        List<Long> festivalPermissionIds = festivalPermissionLowService.findByFestivalId(festivalId)
                .stream()
                .map(permission -> permission.getId())
                .toList();
        festivalPermissionLowService.deleteByFestivalId(festivalId);
        fileService.deleteByDomainIds(festivalPermissionIds, DomainType.FESTIVAL_PERMISSION);
    }

    //admin
    @Transactional(readOnly = true)
    public FestivalPermissionDetailDto getFestivalPermissionById(Long id){
        FestivalPermission festivalPermission = festivalPermissionLowService.findByIdWithFestival(id);
        List<String> docsUrl = getDocumentsUrlById(id);
        return new FestivalPermissionDetailDto(festivalPermission, docsUrl);
    }

    //admin
    @Transactional(readOnly = true)
    public Page<FestivalPermissionAdminListDto> getAllFestivalPermissions(Pageable pageable){
        Page<FestivalPermission> page = festivalPermissionLowService.findAllFestivalPermission(pageable);
        Map<Long, String> departmentsMap = fmPermissionLowService.findDepartmentsByUserIds(page.map(permission -> permission.getUser().getId()).toList());
        List<FestivalPermissionAdminListDto> festivalPermissionAdminList = page
                .map(festivalPermission -> new FestivalPermissionAdminListDto(
                        festivalPermission,
                        departmentsMap.get(festivalPermission.getUser().getId())
                ))
                .toList();
        return new PageImpl<>(festivalPermissionAdminList, pageable, page.getTotalElements());
    }

    //admin
    public void updateFestivalPermissionState(Long id, PermissionState permissionState){
        FestivalPermission festivalPermission = festivalPermissionLowService.findByIdWithFestival(id);
        Festival festival = festivalPermission.getFestival();

        if (permissionState.equals(PermissionState.ACCEPTED))
        {
            if (festival.getManager() != null) { // 이미 매니저가 존재하는 경우
                throw new BadRequestException(ExceptionCode.FESTIVAL_PERMISSION_BAD_REQUEST);
            }
            festival.updateManager(festivalPermission.getUser()); //관리자로 등록
        }
        else{ //PENDING 또는 DENIED인 경우
            if (festival.getManager() != null && festival.getManager().equals(festivalPermission.getUser())) {
                festival.updateManager(null); //관리자로 해제
            }
        }
        festivalPermission.updateState(permissionState);
    }

    private List<String> getDocumentsUrlById(Long id){
        return fileService.findByDomainIdAndDomainType(id, DomainType.FESTIVAL_PERMISSION)
                .stream()
                .map(FileEntity::getUrl)
                .toList();
    }

}
