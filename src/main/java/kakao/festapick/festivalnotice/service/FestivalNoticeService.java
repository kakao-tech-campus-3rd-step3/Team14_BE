package kakao.festapick.festivalnotice.service;

import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.festivalnotice.dto.FestivalNoticeRequestDto;
import kakao.festapick.festivalnotice.dto.FestivalNoticeResponseDto;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FestivalNoticeService {

    private final UserLowService userLowService;
    private final FestivalLowService festivalLowService;
    private final FestivalNoticeLowService festivalNoticeLowService;

    private final FileService fileService;

    public Long addFestivalNotice(Long festivalId, Long userId, FestivalNoticeRequestDto requestDto){
        Festival festival = checkMyFestival(festivalId, userId);
        UserEntity user = userLowService.getReferenceById(userId);
        FestivalNotice festivalNotice = festivalNoticeLowService.save(new FestivalNotice(requestDto, festival, user));
        if(!requestDto.images().isEmpty()){
            //TODO: 연관된 파일을 업로드
        }
        return festivalNotice.getId();
    }

    public FestivalNoticeResponseDto getOne(Long id){
        FestivalNotice festivalNotice = festivalNoticeLowService.findById(id);
        List<String> imageUrls = getFestivalNoticeImages(id);
        return new FestivalNoticeResponseDto(festivalNotice, imageUrls);
    }

    public Page<FestivalNoticeResponseDto> getFestivalNotices(Long festivalId, Pageable pageable){
        Page<FestivalNotice> pagedFestivalNotice = festivalNoticeLowService.findByFestivalId(festivalId, pageable);
        return pagedFestivalNotice.map(fn -> new FestivalNoticeResponseDto(fn, getFestivalNoticeImages(fn.getId())));
    }

    public FestivalNoticeResponseDto updateFestivalNotice(Long id, Long userId, FestivalNoticeRequestDto requestDto){
        FestivalNotice festivalNotice = festivalNoticeLowService.findByIdAndUserId(id, userId);
        festivalNotice.updateTitle(requestDto.title());
        festivalNotice.updateContent(requestDto.content());
        if(!requestDto.images().isEmpty()){
            //파일 업로드 수정
        }
        return new FestivalNoticeResponseDto(festivalNotice, getFestivalNoticeImages(id));
    }

    public void deleteFestivalNotice(Long id, Long userId){
        festivalNoticeLowService.deleteByIdAndUserId(id, userId);
        fileService.deleteByDomainId(id, DomainType.FESTIVAL_NOTICE); //관련 파일 삭제
    }

    private Festival checkMyFestival(Long festivalId, Long userId){
        Festival festival = festivalLowService.findFestivalById(festivalId);
        if(festival.getManager().getId().equals(userId)){
            return festival;
        }
        throw new IllegalStateException("해당 축제에 대해 관리자 권한을 갖고 있지 않습니다.");
    }

    private List<String> getFestivalNoticeImages(Long id){
        List<FileEntity> images = fileService.findByDomainIdAndDomainType(id, DomainType.FESTIVAL_NOTICE);
        return images.stream().map(file -> file.getUrl()).toList();
    }

}
