package kakao.festapick.festival.service;

import java.time.LocalDate;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalLowService {

    private final FestivalRepository festivalRepository;

    public Festival save(Festival festival){
        return festivalRepository.save(festival);
    }

    public void deleteById(Long id){
        festivalRepository.deleteById(id);
    }

    public List<Festival> findAllByState(FestivalState festivalState){
        return festivalRepository.findAllByState(festivalState);
    }

    public Page<Festival> findFestivalByAreaCodeAndDate(int areaCode, LocalDate today, FestivalState state, Pageable pageable){
        return festivalRepository.findFestivalByAreaCodeAndDate(areaCode, today, state, pageable);
    }

    public Festival findFestivalById(Long id) {
        return festivalRepository.findFestivalById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
    }

    public Festival findFestivalByIdWithManager(Long id){
        return festivalRepository.findFestivalByIdWithManager(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
    }

    public Page<Festival> findFestivalByManagerId(Long managerId, Pageable pageable){
        return festivalRepository.findFestivalByManagerId(managerId, pageable);
    }

    public List<Festival> findFestivalByManagerId(Long managerId){
        return festivalRepository.findFestivalByManagerId(managerId);
    }

    public List<Festival> findFestivalsByContentIds(List<String> contentIds){
        return festivalRepository.findFestivalsByContentIds(contentIds);
    }

    public void deleteByManagerId(Long userId){
        festivalRepository.deleteByManagerId(userId);
    }

    public Page<Festival> findFestivalByTitle(String title, FestivalState state, Pageable pageable){
        return festivalRepository.findFestivalByTitle(title, state, pageable);
    }


}
