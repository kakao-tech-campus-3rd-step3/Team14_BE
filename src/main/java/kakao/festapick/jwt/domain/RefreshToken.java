package kakao.festapick.jwt.domain;

import jakarta.persistence.*;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserEntity user;

    public RefreshToken(UserEntity user, String token) {
        this.user = user;
        this.token = token;
    }

}
