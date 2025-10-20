package kakao.festapick.festivalnotice.service;

import kakao.festapick.festivalnotice.Repository.FestivalNoticeRepository;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
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
                .orElseThrow(() -> new ForbiddenException(ExceptionCode.FESTIVAL_NOTICE_ACCESS_FORBIDDEN));
    }

    public Page<FestivalNotice> findByFestivalId(Long id, Pageable pageable){
        return festivalNoticeRepository.findByFestivalId(id, pageable);
    }

    public void deleteByIdAndUserId(Long id, Long userId){
        festivalNoticeRepository.deleteByIdAndUserId(id, userId);
    }

}
