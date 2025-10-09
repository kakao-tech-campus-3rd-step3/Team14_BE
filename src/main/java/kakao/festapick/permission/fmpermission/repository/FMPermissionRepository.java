package kakao.festapick.permission.fmpermission.repository;

import java.util.Optional;
import kakao.festapick.permission.fmpermission.domain.FMPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FMPermissionRepository extends JpaRepository<FMPermission, Long> {

    @Query("select fp from FMPermission fp where fp.user.id =:userId")
    Optional<FMPermission> findByUserId(Long userId);

    void removeFMPermissionByUserId(Long userId);

    boolean existsFMPermissionByUserId(Long userId);

    @Query(
            value = "select fp from FMPermission fp join fetch fp.user",
            countQuery = "select count(fp) from FMPermission fp"
    )
    Page<FMPermission> findAllFMPermissionsWithUser(Pageable pageable);

    Optional<FMPermission> findFMPermissionById(Long id);
}
