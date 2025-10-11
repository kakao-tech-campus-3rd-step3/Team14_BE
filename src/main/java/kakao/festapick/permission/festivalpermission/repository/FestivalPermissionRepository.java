package kakao.festapick.permission.festivalpermission.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FestivalPermissionRepository extends JpaRepository<FestivalPermission, Long> {

    @Query("select fp from FestivalPermission fp join fetch fp.festival where fp.user.id =:userId")
    Page<FestivalPermission> findFestivalPermissionsByUserIdWithFestival(Long userId, Pageable pageable);

    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    Optional<FestivalPermission> findByIdAndUserId(Long id, Long userId);

    @Query("select fp from FestivalPermission fp join fetch fp.festival where fp.id =:id and fp.user.id =:userId")
    Optional<FestivalPermission> findByIdAndUserIdWithFestival(Long id, Long userId);

    @Query("select fp from FestivalPermission fp join fetch fp.festival where fp.id =:id")
    Optional<FestivalPermission> findByIdWithFestival(Long id);

    @Query("select fp from FestivalPermission fp join fetch fp.user join fetch fp.festival")
    List<FestivalPermission> findAllFestivalPermission();

    void removeById(Long id);
    
}
