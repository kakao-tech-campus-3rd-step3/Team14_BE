package kakao.festapick.wish.domain;

import jakarta.persistence.*;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
public class Wish {

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

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

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
