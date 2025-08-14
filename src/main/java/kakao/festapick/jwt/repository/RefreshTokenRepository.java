package kakao.festapick.jwt.repository;

import kakao.festapick.jwt.domain.RefreshToken;
import kakao.festapick.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.user = :user")
    void deleteByUser(UserEntity user);

    UserEntity user(UserEntity user);
}
