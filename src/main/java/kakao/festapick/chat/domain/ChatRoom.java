package kakao.festapick.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kakao.festapick.festival.domain.Festival;
import lombok.Getter;

@Entity
@Getter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roomName", nullable = false, length = 255)
    private String roomName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    protected ChatRoom() {
    }

    public ChatRoom(String roomName, Festival festival) {
        this(null, roomName, festival);
    }

    public ChatRoom(Long id, String roomName, Festival festival) {
        this.id = id;
        this.roomName = roomName;
        this.festival = festival;
    }

    public Long getFestivalId() {
        return this.festival.getId();
    }
}
