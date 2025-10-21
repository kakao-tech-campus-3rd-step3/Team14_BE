package kakao.festapick.festivalnotice.service;

import java.util.List;
import kakao.festapick.festivalnotice.Repository.FestivalNoticeRepository;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalNoticeLowService {

    private final FestivalNoticeRepository festivalNoticeRepository;

    public FestivalNotice save(FestivalNotice festivalNotice){
        return festivalNoticeRepository.save(festivalNotice);
    }

    public FestivalNotice findByIdAndAuthorId(Long id, Long userId){
        return festivalNoticeRepository.findByIdAndAuthorId(id, userId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOTICE_NOT_FOUND));
    }

    public Page<FestivalNotice> findPagedNoticeByFestivalId (Long id, Pageable pageable){
        return festivalNoticeRepository.findByFestivalId(id, pageable);
    }

    public void deleteByIdAndUserId(Long id, Long userId){
        festivalNoticeRepository.deleteByIdAndUserId(id, userId);
    }

    public List<FestivalNotice> findByFestivalId(Long id){
        return festivalNoticeRepository.findByFestivalId(id);
    }

    public List<FestivalNotice> findByUserId(Long userId){
        return festivalNoticeRepository.findByUserId(userId);
    }

    public void deleteByUserId(Long userId){
        festivalNoticeRepository.deleteByUserId(userId);
    }

    public void deleteByFestivalId(Long festivalId){
        festivalNoticeRepository.deleteByFestivalId(festivalId);
    }

}
