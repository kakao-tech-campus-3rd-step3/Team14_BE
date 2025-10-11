package kakao.festapick.wish.domain;

import jakarta.persistence.*;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
public class Wish extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wish_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    protected Wish() {
    }

    public Wish(UserEntity user, Festival festival) {
        this(null, user, festival);
    }

    public Wish(Long id, UserEntity user, Festival festival) {
        this.id = id;
        this.user = user;
        this.festival = festival;
    }
}
