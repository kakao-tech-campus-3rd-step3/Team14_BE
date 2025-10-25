package kakao.festapick.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.festival.domain.Festival;
import lombok.Getter;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_chat_room_festival_id", columnList = "festival_id"))
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roomName", nullable = false, length = 255)
    private String roomName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false, unique = true)
    private Festival festival;

    @Version
    private Long version;

    @Column(nullable = false)
    private Long messageSeq;

    protected ChatRoom() {
    }

    public ChatRoom(String roomName, Festival festival) {
        this.roomName = roomName;
        this.festival = festival;
        this.messageSeq = 0L;
    }

    public Long getFestivalId() {
        return this.festival.getId();
    }

    public void updateMessageSeq() {
        this.messageSeq = (this.messageSeq == null) ? 1L : this.messageSeq + 1L;
    }
}
