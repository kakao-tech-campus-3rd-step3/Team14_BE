package kakao.festapick.fileupload.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @Column(nullable = false)
    private Long domainId;

    public FileEntity(String url, FileType fileType, DomainType domainType, Long domainId) {
        this.url = url;
        this.fileType = fileType;
        this.domainType = domainType;
        this.domainId = domainId;
    }
}
