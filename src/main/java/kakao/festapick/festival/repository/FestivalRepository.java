package kakao.festapick.festival.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    List<Festival> findAllByState(FestivalState state);

    Optional<Festival> findFestivalById(Long id);

    Page<Festival> findFestivalByManagerIdAndState(Long managerId, FestivalState state, Pageable pageable);

    List<Festival> findFestivalByManagerId(Long managerId);

    @Query("select f from Festival f where f.manager.id = :managerId and f.festivalType = :festivalType")
    Page<Festival> findCustomFestivalByManagerId(Long managerId, FestivalType festivalType, Pageable pageable);

    boolean existsFestivalById(Long id);

    @Query("select f from Festival f where f.contentId in :contentIds")
    List<Festival> findFestivalsByContentIds(List<String> contentIds);

    @Modifying(clearAutomatically = true)
    @Query("delete from Festival f where f.manager.id = :userId")
    void deleteByManagerId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Festival f where f.manager.id =:userId and f.festivalType =:festivalType")
    void deleteCustomFestivalByUserIdAndFestivalType(Long userId, FestivalType festivalType);

    @Query("select f from Festival f where f.state = :state and f.title like concat('%', :title, '%')")
    Page<Festival> findFestivalByTitle(String title, FestivalState state, Pageable pageable);

    @Query("select f from Festival f left join fetch f.reviews where f.id = :festivalId")
    Optional<Festival> findByIdWithReviews(Long festivalId);

}
