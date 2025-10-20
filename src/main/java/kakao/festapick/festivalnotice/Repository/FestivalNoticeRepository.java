package kakao.festapick.festivalnotice.Repository;

import java.util.List;
import java.util.Optional;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FestivalNoticeRepository extends JpaRepository<FestivalNotice, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from FestivalNotice fn where fn.id =:id and fn.author.id =:userId")
    void deleteByIdAndUserId(Long id, Long userId);

    Optional<FestivalNotice> findByIdAndAuthorId(Long id, Long authorId);

    Page<FestivalNotice> findByFestivalId(Long festivalId, Pageable pageable);

    List<FestivalNotice> findByFestivalId(Long festivalId);

    @Modifying(clearAutomatically = true)
    @Query("delete from FestivalNotice fn where fn.author.id =:userId")
    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from FestivalNotice fn where fn.festival.id =:festivalId")
    void deleteByFestivalId(Long festivalId);
}
