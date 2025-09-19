package kakao.festapick.fileupload.repository;

import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> { ;

    @Query("select f from FileEntity f where f.domainId in (:domainIds) and f.domainType = :domainType")
    List<FileEntity> findByDomainIdsAndDomainType(List<Long> domainIds, DomainType domainType);

    @Query("select f from FileEntity f where f.domainId = :domainId and f.domainType = :domainType")
    List<FileEntity> findByDomainIdAndDomainType(Long domainId, DomainType domainType);

    @Modifying
    @Query("delete from FileEntity f where f.domainId = :domainId and f.domainType = :domainType ")
    void deleteByDomainIdAndDomainType(Long domainId, DomainType domainType);

    @Modifying
    @Query("delete from FileEntity f where f.domainId in (:domainIds) and f.domainType = :domainType")
    void deleteByDomainIdsAndDomainType(List<Long> domainIds, DomainType domainType);

    @Query("select f from FileEntity f where f.url in :urls")
    List<FileEntity> findByUrls(List<String> urls);
}
