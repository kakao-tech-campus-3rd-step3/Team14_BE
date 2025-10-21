package kakao.festapick.festivalnotice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.festivalnotice.dto.FestivalNoticeRequestDto;
import kakao.festapick.festivalnotice.dto.FestivalNoticeResponseDto;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.FileUploadHelper;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
import kakao.festapick.global.exception.NotFoundEntityException;
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
    private final FileUploadHelper fileUploadHelper;

    public Long addFestivalNotice(Long festivalId, Long userId, FestivalNoticeRequestDto requestDto){
        Festival festival = checkMyFestival(festivalId, userId);
        UserEntity user = userLowService.getReferenceById(userId);
        Long festivalNoticeId = festivalNoticeLowService.save(new FestivalNotice(requestDto, festival, user)).getId();
        if(requestDto.images() != null && !(requestDto.images().isEmpty())){
            fileUploadHelper.saveFiles(requestDto.images(), festivalNoticeId, FileType.IMAGE, DomainType.FESTIVAL_NOTICE);
        }
        return festivalNoticeId;
    }

    @Transactional(readOnly = true)
    public Page<FestivalNoticeResponseDto> getFestivalNotices(Long festivalId, Pageable pageable){
        if(festivalLowService.existsFestivalById(festivalId)){
            Page<FestivalNotice> pagedFestivalNotice = festivalNoticeLowService.findPagedNoticeByFestivalId(festivalId, pageable);
            List<Long> noticeIds = pagedFestivalNotice.map(FestivalNotice::getId).stream().toList();
            Map<Long, List<String>> noticeImages = getFestivalNoticeImages(noticeIds);
            return pagedFestivalNotice.map(fn -> new FestivalNoticeResponseDto(fn, noticeImages.getOrDefault(fn.getId(), List.of())));
        }
        throw new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND);
    }

    public FestivalNoticeResponseDto updateFestivalNotice(Long id, Long userId, FestivalNoticeRequestDto requestDto){
        FestivalNotice festivalNotice = festivalNoticeLowService.findByIdAndAuthorId(id, userId);
        festivalNotice.updateTitle(requestDto.title());
        festivalNotice.updateContent(requestDto.content());
        if(requestDto.images() != null){
            fileUploadHelper.updateFiles(id, DomainType.FESTIVAL_NOTICE, FileType.IMAGE, requestDto.images());
        }
        return new FestivalNoticeResponseDto(festivalNotice, getFestivalNoticeImage(id));
    }

    public void deleteFestivalNotice(Long id, Long userId){
        festivalNoticeLowService.deleteByIdAndUserId(id, userId);
        fileService.deleteByDomainId(id, DomainType.FESTIVAL_NOTICE); //관련 파일 삭제
    }

    // User가 탈퇴한 경우
    public void deleteByUserId(Long userId){
        List <Long> domainIds = festivalNoticeLowService.findByUserId(userId)
                .stream()
                .map(FestivalNotice::getId)
                .toList();
        festivalNoticeLowService.deleteByUserId(userId);
        fileService.deleteByDomainIds(domainIds, DomainType.FESTIVAL_NOTICE);
    }

    // 축제가 삭제된 경우
    public void deleteByFestivalId(Long festivalId){
        List<Long> domainIds = festivalNoticeLowService.findByFestivalId(festivalId)
                .stream()
                .map(FestivalNotice::getId)
                .toList();
        festivalNoticeLowService.deleteByFestivalId(festivalId);
        fileService.deleteByDomainIds(domainIds, DomainType.FESTIVAL_NOTICE);
    }

    private Festival checkMyFestival(Long festivalId, Long userId){
        Festival festival = festivalLowService.findFestivalById(festivalId);
        if(festival.getManager() != null && festival.getManager().getId().equals(userId)){
            return festival;
        }
        throw new ForbiddenException(ExceptionCode.FESTIVAL_NOTICE_ACCESS_FORBIDDEN);
    }

    private List<String> getFestivalNoticeImage(Long id){
        List<FileEntity> images = fileService.findByDomainIdAndDomainType(id, DomainType.FESTIVAL_NOTICE);
        return images.stream().map(file -> file.getUrl()).toList();
    }

    private Map<Long, List<String>> getFestivalNoticeImages(List<Long> ids){
        Map<Long, List<String>> noticeImages = new HashMap<>();
        for(FileEntity file : fileService.findAllFileEntityByDomain(ids, DomainType.FESTIVAL_NOTICE)){
            Long key = file.getDomainId();
            if(!noticeImages.containsKey(key)){
                noticeImages.put(key, new ArrayList<>());
            }
            noticeImages.get(key).add(file.getUrl());
        }
        return noticeImages;
    }

}
