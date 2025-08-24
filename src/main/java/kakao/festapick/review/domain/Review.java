package kakao.festapick.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;
import org.hibernate.annotations.Check;

@Entity
@Getter
@Check(constraints = "score >= 1 AND score <= 5")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "score", nullable = false)
    @Min(1)
    @Max(5)
    private Integer score;

    protected Review() {
    }

    public Review(UserEntity user, Festival festival, String content, Integer score) {
        this(null, user, festival, content, score);
    }

    public Review(Long id, UserEntity user, Festival festival, String content, Integer score) {
        this.id = id;
        this.user = user;
        this.festival = festival;
        this.content = content;
        this.score = score;
    }

    public String getFestivalTitle() {
        return this.festival.getTitle();
    }

    public String getReviewerName() {
        return this.user.getUsername();
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeScore(Integer score) {
        this.score = score;
    }
}