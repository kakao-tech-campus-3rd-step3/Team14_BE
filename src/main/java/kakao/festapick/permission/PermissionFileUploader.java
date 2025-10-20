package kakao.festapick.permission;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionFileUploader {

    private final FileService fileService;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;

    public void saveFiles(List<FileUploadRequest> documents, Long id, DomainType domainType) {
        List<FileEntity> files = new ArrayList<>();
        List<Long> temporalFileIds = new ArrayList<>();

        documents.forEach(
                docInfo -> {
                    files.add(new FileEntity(docInfo.presignedUrl(), FileType.DOCUMENT, domainType, id));
                    temporalFileIds.add(docInfo.id());
                }
        );

        fileService.saveAll(files);
        temporalFileRepository.deleteByIds(temporalFileIds);
    }

    public void updateFiles(Long domainId, DomainType domainType, List<FileUploadRequest> documents){
        List<FileEntity> registeredDocs = fileService.findByDomainIdAndDomainType(domainId, domainType);
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

        saveFiles(uploadFiles, domainId, domainType); //db에 저장
        fileService.deleteAllByFileEntity(deleteFiles); //db에서 삭제
        s3Service.deleteFiles(deleteDocsUrl.stream().toList()); //s3에서 삭제
    }

}
