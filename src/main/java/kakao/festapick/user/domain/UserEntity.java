package kakao.festapick.user.domain;

import jakarta.persistence.*;
import kakao.festapick.jwt.domain.RefreshToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
@Getter
public class UserEntity {

    private static final String defaultImage
            = "https://festapick-file.s3.ap-northeast-2.amazonaws.com/defaultImage/userDefaultImage.jpeg";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoleType roleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = false)
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshToken;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    public UserEntity(String identifier, String email, String username, UserRoleType userRoleType, SocialType socialType) {
        this.identifier = identifier;
        this.email = email;
        this.username = username;
        this.roleType = userRoleType;
        this.socialType = socialType;
        this.profileImageUrl = defaultImage;
    }

    public UserEntity(Long id, String identifier, String email, String username, UserRoleType roleType, SocialType socialType) {
        this.id = id;
        this.identifier = identifier;
        this.email = email;
        this.username = username;
        this.roleType = roleType;
        this.socialType = socialType;
        this.profileImageUrl = defaultImage;
    }

    public void changeUserRole(UserRoleType roleType) {
        this.roleType = roleType;
    }

    public void changeProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }
}
