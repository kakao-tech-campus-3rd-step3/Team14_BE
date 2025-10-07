package kakao.festapick.fileupload.domain;

import jakarta.persistence.*;
import kakao.festapick.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemporalFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    public TemporalFile(String url) {
        this.url = url;
    }

    public TemporalFile(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
