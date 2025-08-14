package kakao.festapick.user.repository;

import kakao.festapick.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByIdentifier(String identifier);

    void deleteByIdentifier(String identifier);
}
