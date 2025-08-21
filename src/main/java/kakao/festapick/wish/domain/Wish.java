package kakao.festapick.wish.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wish_id")
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
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
