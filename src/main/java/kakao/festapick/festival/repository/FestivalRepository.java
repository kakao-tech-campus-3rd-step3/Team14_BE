package kakao.festapick.festival.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    //TourAPI를 통해 데이터를 가져올 때, 중복을 확인하기 위함
    Optional<Festival> findFestivalByContentId(String contentId);

    List<Festival> findAllByState(FestivalState state);

    @Query("select f from Festival f where f.areaCode = ?1 and f.startDate <= ?2 and ?2 <= f.endDate and f.state = ?3")
    Page<Festival> findFestivalByAreaCodeAndDate(int areaCode, LocalDate today, FestivalState state, Pageable pageable);

    Optional<Festival> findFestivalById(Long id);

    @Query("select f from Festival f left join fetch f.manager where f.id= :id")
    Optional<Festival> findFestivalByIdWithManager(Long id);

    //pending
    List<Festival> findFestivalByTitleContainingAndState(String title, FestivalState state);
    List<Festival> findFestivalByAreaCodeAndState(int areaCode, FestivalState state);

}
