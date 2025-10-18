package kakao.festapick.festivalnotice.service;

import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festivalnotice.Repository.FestivalNoticeRepository;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.festivalnotice.dto.FestivalNoticeResponseDto;
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
                .orElseThrow(() ->  new IllegalStateException("내가 작성한 공지사항이 아닙니다."));
    }

    public FestivalNotice findById(Long id){
        return festivalNoticeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항 입니다."));
    }

    public Page<FestivalNotice> findByFestivalId(Long id, Pageable pageable){
        return festivalNoticeRepository.findByFestivalId(id, pageable);
    }

    public void deleteByIdAndUserId(Long id, Long userId){
        festivalNoticeRepository.deleteByIdAndUserId(id, userId);
    }

}
