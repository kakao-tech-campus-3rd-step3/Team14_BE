package kakao.festapick.fileupload.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_domainId_domainType", columnList = "domainId, domainType"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @Column(nullable = false)
    private Long domainId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    public FileEntity(String url, FileType fileType, DomainType domainType, Long domainId) {
        this.url = url;
        this.fileType = fileType;
        this.domainType = domainType;
        this.domainId = domainId;
    }
}
