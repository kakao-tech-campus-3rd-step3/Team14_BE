package kakao.festapick.chat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "participantUniqueConstraint",
                columnNames = {"user_id", "chatroom_id"}
        )
},
        indexes = @Index(name = "idx_chat_participant_user_id_chatroom_id", columnList = "user_id, chatroom_id"))
public class ChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoom chatRoom;

    private Long version;

    protected ChatParticipant() {
    }

    public ChatParticipant(UserEntity user, ChatRoom chatRoom) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.version = chatRoom.getVersion();
    }

    public void syncVersion() {
        this.version = this.chatRoom.getVersion();
    }
}
