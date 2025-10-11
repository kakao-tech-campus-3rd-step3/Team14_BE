package kakao.festapick.permission.festivalpermission.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
public class FestivalPermission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Enumerated(value = EnumType.STRING)
    private PermissionState permissionState;

    public FestivalPermission(UserEntity user, Festival festival){
        this.user = user;
        this.festival = festival;
        this.permissionState = PermissionState.PENDING;
    }

    public void updateState(PermissionState permissionState){
        this.permissionState = permissionState;
    }

    protected FestivalPermission(){}

}
