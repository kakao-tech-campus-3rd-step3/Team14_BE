package kakao.festapick.permission.fmpermission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
public class FMPermission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    private PermissionState permissionState;

    public FMPermission(UserEntity user, String department){
        this.user = user;
        this.department = department;
        permissionState = PermissionState.PENDING;
    }

    protected FMPermission(){}

    public void updateState(PermissionState permissionState){
        this.permissionState = permissionState;
    }

}
