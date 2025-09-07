package kakao.festapick.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "content", nullable = false, length = 500)
    @Size(min = 10, max = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime chatDateTime;

    protected Chat () {}

    public Chat(UserEntity user, Festival festival, String content, LocalDateTime chatDateTime) {
        this(null, user, festival, content, chatDateTime);
    }

    public Chat(Long id, UserEntity user, Festival festival, String content, LocalDateTime chatDateTime) {
        this.id = id;
        this.user = user;
        this.festival = festival;
        this.content = content;
        this.chatDateTime = chatDateTime;
    }
}
