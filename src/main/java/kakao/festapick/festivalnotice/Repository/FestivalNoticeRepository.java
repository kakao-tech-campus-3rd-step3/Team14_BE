package kakao.festapick.festivalnotice.Repository;

import jakarta.persistence.Id;
import java.util.Optional;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FestivalNoticeRepository extends JpaRepository<FestivalNotice, Id> {

    @Modifying(clearAutomatically = true)
    @Query("delete from FestivalNotice fn where fn.id =:id and fn.user.id =:userId")
    void deleteByIdAndUserId(Long id, Long userId);

    Optional<FestivalNotice> findByIdAndUserId(Long id, Long userId);

    Optional<FestivalNotice> findById(Long id);

    Page<FestivalNotice> findByFestivalId(Long festivalId, Pageable pageable);
}
