package kakao.festapick.festival.repository;

import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    List<Festival> findAllByState(FestivalState state);

    @Query("select f from Festival f where f.areaCode = :areaCode and :today <= f.endDate and f.state = :state")
    Page<Festival> findFestivalByAreaCodeAndDate(int areaCode, LocalDate today, FestivalState state, Pageable pageable);

    Optional<Festival> findFestivalById(Long id);

    @Query("select f from Festival f left join fetch f.manager where f.id= :id")
    Optional<Festival> findFestivalByIdWithManager(Long id);

    Page<Festival> findFestivalByManagerId(Long managerId, Pageable pageable);

    List<Festival> findFestivalByManagerId(Long managerId);

    @Query("select f from Festival f where f.contentId in :contentIds")
    List<Festival> findFestivalsByContentIds(List<String> contentIds);

    @Modifying(clearAutomatically = true)
    @Query("delete from Festival f where f.manager.id = :userId")
    void deleteByManagerId(Long userId);

    @Query("select f from Festival f where f.state = :state and f.title like concat('%', :title, '%')")
    Page<Festival> findFestivalByTitle(String title, FestivalState state, Pageable pageable);

}
