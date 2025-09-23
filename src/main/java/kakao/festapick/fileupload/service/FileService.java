package kakao.festapick.fileupload.service;

import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.repository.FileRepository;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final S3Service s3Service;


    public void saveAll(List<FileEntity> files) {
        fileRepository.saveAll(files);
    }

    public List<FileEntity> findAllFileEntityByDomain(List<Long> domainIds, DomainType domainType) {

       return fileRepository.findByDomainIdsAndDomainType(domainIds, domainType);
    }

    public List<FileEntity> findByDomainIdAndDomainType(Long domainId, DomainType domainType) {
        return fileRepository.findByDomainIdAndDomainType(domainId, domainType);
    }

    public void deleteAllByFileEntity(List<FileEntity> files) {
        fileRepository.deleteAll(files);
    }

    public void deleteByDomainId(Long domainId, DomainType domainType) {

        List<String> urls = fileRepository.findByDomainIdAndDomainType(domainId, domainType)
                .stream()
                .map(FileEntity::getUrl).toList();

        fileRepository.deleteByDomainIdAndDomainType(domainId, domainType);

        s3Service.deleteFiles(urls); // s3 파일 삭제는 항상 마지막에 호출
    }

    public void deleteByDomainIds(List<Long> domainIds, DomainType domainType) {

        List<String> urls = fileRepository.findByDomainIdsAndDomainType(domainIds, domainType)
                .stream()
                .map(FileEntity::getUrl).toList();

        fileRepository.deleteByDomainIdsAndDomainType(domainIds, domainType);

        s3Service.deleteFiles(urls); // s3 파일 삭제는 항상 마지막에 호출

    }

    public void checkUniqueURL(List<String> imagesUrl){
        if(!fileRepository.findByUrls(imagesUrl).isEmpty()){
            throw new BadRequestException(ExceptionCode.FESTIVAL_BAD_IMAGE);
        }
    }

}
