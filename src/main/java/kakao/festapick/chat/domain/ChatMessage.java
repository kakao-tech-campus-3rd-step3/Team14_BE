package kakao.festapick.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_chat_message_chatroom_id_user_id", columnList = "chatroom_id, user_id"))
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, length = 255)
    @Size(max = 255)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    protected ChatMessage() {
    }

    public ChatMessage(String content, ChatRoom chatRoom, UserEntity user) {
        this(null, content, chatRoom, user);
    }

    public ChatMessage(Long id, String content, ChatRoom chatRoom, UserEntity user) {
        this.id = id;
        this.content = content;
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public String getSenderName() {
        return this.getUser().getUsername();
    }

    public String getSenderProfileUrl() {
        return this.getUser().getProfileImageUrl();
    }
}
