package kakao.festapick.festival.repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Optional<Festival> findFestivalByContentIdAndState(String contentId, FestivalState state);

    void removeFestivalById(Long id);

    List<Festival> findAllByState(FestivalState state);

    @Query("select f from Festival f where f.areaCode = ?1 and f.startDate <= ?2 and ?2 <= f.endDate and f.state = ?3")
    List<Festival> findFestivalByAreaCodeAndDate(String areaCode, String today, FestivalState state);

    Optional<Festival> findFestivalByIdAndState(Long id, FestivalState state);

    List<Festival> findFestivalByTitleContainingAndState(String title, FestivalState state);

    List<Festival> findFestivalByAreaCodeAndState(String areaCode, FestivalState state);

    Optional<Festival> findFestivalById(Long id);
}
