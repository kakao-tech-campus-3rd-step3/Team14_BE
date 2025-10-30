package kakao.festapick.festival.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.repository.QFestivalRepository;
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

    private final QFestivalRepository qFestivalRepository;

    public Festival save(Festival festival){
        return festivalRepository.save(festival);
    }

    public void deleteById(Long id){
        festivalRepository.deleteById(id);
    }

    public List<Festival> findAllByState(FestivalState festivalState){
        return festivalRepository.findAllByState(festivalState);
    }

    public Page<Festival> findFestivalByAreaCodeAndDate(Integer areaCode, LocalDate today, Pageable pageable){
        return qFestivalRepository.findFestivalByAreaCodeAndDate(areaCode, today, pageable);
    }

    public Festival findFestivalById(Long id) {
        return festivalRepository.findFestivalById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
    }

    public Page<Festival> findFestivalByManagerId(Long managerId, Pageable pageable){
        return festivalRepository.findFestivalByManagerIdAndState(managerId, FestivalState.APPROVED, pageable);
    }

    public List<Festival> findFestivalByManagerId(Long managerId){
        return festivalRepository.findFestivalByManagerId(managerId);
    }

    public Page<Festival> findCustomFestivalByManagerId(Long managerId, Pageable pageable){
        return festivalRepository.findCustomFestivalByManagerId(managerId, FestivalType.FESTAPICK, pageable);
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

    public Page<Festival> findByStateAndTitleLike(FestivalSearchCondForAdmin cond, Pageable pageable){
        return qFestivalRepository.findByStateAndTitleLike(cond, pageable);
    }

    public List<Festival> findAllById(Set<Long> ids){
        return festivalRepository.findAllById(ids);
    }

    public Festival findByIdWithReviews(Long id) {
        return festivalRepository.findByIdWithReviews(id)
                .orElseThrow(()-> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
    }

    public Boolean existsFestivalById(Long id){
        return festivalRepository.existsFestivalById(id);
    }

    public Festival getReferenceById(Long id) {
        return festivalRepository.getReferenceById(id);
    }

}
