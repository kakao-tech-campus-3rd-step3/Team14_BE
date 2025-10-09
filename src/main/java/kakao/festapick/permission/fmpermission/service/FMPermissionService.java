package kakao.festapick.permission.fmpermission.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.permission.PermissionState;
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

    private final FileService fileService;
    private final TemporalFileRepository temporalFileRepository;
    private final S3Service s3Service;

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
        saveFiles(documents, fmPermissionId);
        return fmPermissionId;
    }

    // 업데이트(반려 또는 대기 중인 경우 서류를 수정하도록)
    public FMPermissionResponseDto modifyDocuments(Long userId, Long id, List<FileUploadRequest> documents){

        FMPermission fmPermission = fmPermissionLowService.findFMPermissionByUserId(userId);

        if(fmPermission.getPermissionState().equals(PermissionState.ACCEPTED)){
            throw new BadRequestException(ExceptionCode.FM_PERMISSION_BAD_REQUEST);
        }

        List<FileEntity> registeredDocs = fileService.findByDomainIdAndDomainType(id, DomainType.PERMISSION);
        Set<String> registeredDocsUrl = registeredDocs.stream()
                .map(docs -> docs.getUrl())
                .collect(Collectors.toSet());

        Set<String> requestDocsUrl = documents.stream()
                .map(docs -> docs.presignedUrl())
                .collect(Collectors.toSet());

        Set<String> deleteDocsUrl = new HashSet<>(registeredDocsUrl);
        deleteDocsUrl.removeAll(requestDocsUrl); //삭제해야할 문서의 링크
        List<FileEntity> deleteFiles = registeredDocs.stream()
                .filter(fileEntity -> deleteDocsUrl.contains(fileEntity.getUrl()))
                .toList();

        Set<String> uploadDocsUrl = new HashSet<>(requestDocsUrl);
        uploadDocsUrl.removeAll(registeredDocsUrl); //업로드해야할 문서의 링크
        List<FileUploadRequest> uploadFiles = documents.stream()
                .filter(fileUploadRequest -> uploadDocsUrl.contains(fileUploadRequest.presignedUrl()))
                .toList();

        saveFiles(uploadFiles, id); //db에 저장
        fileService.deleteAllByFileEntity(deleteFiles); //db에서 삭제
        s3Service.deleteFiles(deleteDocsUrl.stream().toList()); //s3에서 삭제

        fmPermission.updateState(PermissionState.PENDING);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(fmPermission.getId(), DomainType.PERMISSION)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();

        return new FMPermissionResponseDto(fmPermission, docsUrl);
    }

    //조회
    @Transactional(readOnly = true)
    public FMPermissionResponseDto getFMPermissionByUserId(Long userId){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionByUserId(userId);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(fmPermission.getId(), DomainType.PERMISSION)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();
        return new FMPermissionResponseDto(fmPermission, docsUrl);
    }

    @Transactional(readOnly = true)
    public FMPermissionResponseDto getFMPermissionById(Long id){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionById(id);
        List<String> docsUrl = fileService.findByDomainIdAndDomainType(fmPermission.getId(), DomainType.PERMISSION)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();
        return new FMPermissionResponseDto(fmPermission, docsUrl);
    }

    //삭제
    public void removeMyFMPermission(Long userId, Long id){

        if(!fmPermissionLowService.existsByUserId(userId)){
            throw new NotFoundEntityException(ExceptionCode.FM_PERMISSION_NOT_FOUND);
        }

        fmPermissionLowService.removeFMPermission(userId);
        fileService.deleteByDomainId(id, DomainType.PERMISSION);//첨부 했던 모든 문서들 삭제
    }

    //admin - 전체 조회
    @Transactional(readOnly = true)
    public Page<FMPermissionAdminListResponseDto> getAllFMPermission(Pageable pageable){
        return fmPermissionLowService.findAll(pageable).map(FMPermissionAdminListResponseDto::new);
    }

    //admin - 승인
    public void updateState(Long id, PermissionState permissionState){
        FMPermission fmPermission = fmPermissionLowService.findFMPermissionById(id);
        fmPermission.updateState(permissionState);

        if(permissionState.equals(PermissionState.ACCEPTED)){
            UserEntity user = fmPermission.getUser();
            user.changeUserRole(UserRoleType.FESTIVAL_MANAGER); //UserRole 변경
        }
    }

    private void saveFiles(List<FileUploadRequest> documents, Long id) {
        List<FileEntity> files = new ArrayList<>();
        List<Long> temporalFileIds = new ArrayList<>();

        documents.forEach(
                docInfo -> {
                    files.add(new FileEntity(docInfo.presignedUrl(), FileType.DOCUMENT, DomainType.PERMISSION, id));
                    temporalFileIds.add(docInfo.id());
                }
        );

        fileService.saveAll(files);
        temporalFileRepository.deleteByIds(temporalFileIds);
    }

}
