package kakao.festapick.permission.festivalpermission.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FestivalPermissionRepository extends JpaRepository<FestivalPermission, Long> {

    @Query(
            value = "select fp from FestivalPermission fp join fetch fp.festival where fp.user.id =:userId",
            countQuery = "select count (fp) from FestivalPermission fp where fp.user.id =:userId"
    )
    Page<FestivalPermission> findFestivalPermissionsByUserIdWithFestival(Long userId, Pageable pageable);

    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    Optional<FestivalPermission> findByIdAndUserId(Long id, Long userId);

    @Query("select fp from FestivalPermission fp join fetch fp.festival where fp.id =:id and fp.user.id =:userId")
    Optional<FestivalPermission> findByIdAndUserIdWithFestival(Long id, Long userId);

    @Query("select fp from FestivalPermission fp join fetch fp.festival where fp.id =:id")
    Optional<FestivalPermission> findByIdWithFestival(Long id);

    @Query(
            value = "select fp from FestivalPermission fp join fetch fp.user join fetch fp.festival",
            countQuery = "select count(fp) from FestivalPermission fp"
    )
    Page<FestivalPermission> findAllFestivalPermission(Pageable pageable);

    void deleteById(Long id);

    @Modifying(clearAutomatically = true)
    @Query("delete from FestivalPermission fp where fp.user.id =:userId")
    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from FestivalPermission fp where fp.festival.id =:festivalId")
    void deleteByFestivalId(Long festivalId);

    List<FestivalPermission> findByUserId(Long userId);

    @Query("select fp from FestivalPermission fp join fetch fp.festival where fp.user.id =:userId")
    List<FestivalPermission> findByUserIdWithFestival(Long userId);

    List<FestivalPermission> findByFestivalId(Long festivalId);
}
