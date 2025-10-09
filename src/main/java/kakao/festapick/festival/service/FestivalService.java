package kakao.festapick.festival.service;

import kakao.festapick.ai.service.RecommendationHistoryLowService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.*;
import kakao.festapick.festival.repository.QFestivalRepository;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
import kakao.festapick.review.service.ReviewService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.wish.service.WishLowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalLowService festivalLowService;
    private final WishLowService wishLowService;
    private final ReviewService reviewService;
    private final UserLowService userLowService;
    private final RecommendationHistoryLowService recommendationHistoryLowService;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;
    private final FileService fileService;
    private final ChatRoomService chatRoomService;

    //CREATE
    @Transactional
    public Long addCustomizedFestival(FestivalCustomRequestDto requestDto, Long userId) {
        UserEntity user = userLowService.getReferenceById(userId);

        Festival festival = new Festival(requestDto, user);
        Festival savedFestival = festivalLowService.save(festival);
        
        temporalFileRepository.deleteById(requestDto.posterInfo().id());

        //관련 이미지 업로드(포스터의 경우에는 festival 도메인에서만 관리)
        if(requestDto.imageInfos() != null){
            //이미지에 대한 중복 체크(url : unique 속성)
            List<String> imageUrls = requestDto.imageInfos().stream().map(FileUploadRequest::presignedUrl).toList();
            fileService.checkUniqueURL(imageUrls);
            saveFiles(requestDto.imageInfos(), savedFestival.getId());
        }

        return savedFestival.getId();
    }

    //Id를 통한 축제 조회
    public FestivalDetailResponseDto findOneById(Long festivalId) {
        Festival festival = festivalLowService.findFestivalById(festivalId);
        List<String> images = fileService.findByDomainIdAndDomainType(festivalId, DomainType.FESTIVAL)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();
        return new FestivalDetailResponseDto(festival, images);
    }

    //내가 등록한 축제를 조회
    public Page<FestivalListResponse> findMyFestivals(Long userId, Pageable pageable){

        return festivalLowService.findFestivalByManagerId(userId, pageable)
                .map(FestivalListResponse::new);
    }

    //지역코드와 날짜(오늘)를 통해 승인된 축제를 조회
    public Page<FestivalListResponse> findApprovedAreaAndDate(int areaCode, Pageable pageable) {
        Page<Festival> festivalList = festivalLowService.findFestivalByAreaCodeAndDate(areaCode,
                LocalDate.now(), FestivalState.APPROVED, pageable);
        return festivalList.map(FestivalListResponse::new);
    }

    public Page<FestivalListResponse> findFestivalByTitle(String keyWord, Pageable pageable){
        Page<Festival> festivals = festivalLowService.findFestivalByTitle(keyWord, FestivalState.APPROVED, pageable);
        return festivals.map(FestivalListResponse::new);
    }

    //모든 축제 검색 기능(관리자)
    public Page<FestivalListResponseForAdmin> findAllWithPage(FestivalSearchCondForAdmin cond,
            Pageable pageable) {
        return festivalLowService.findByStateAndTitleLike(cond, pageable)
                .map(FestivalListResponseForAdmin::new);
    }

    //UPDATE
    //축제 정보를 업데이트(축제 관리자)
    @Transactional
    public FestivalDetailResponseDto updateFestival(Long userId, Long id, FestivalUpdateRequestDto requestDto) {

        Festival festival = checkMyFestival(userId, id);

        // 기존 이미지 파일들
        List<FileEntity> registeredImages = fileService.findByDomainIdAndDomainType(festival.getId(), DomainType.FESTIVAL);
        Set<String> registeredImgUrl = registeredImages.stream()
                .map(FileEntity::getUrl)
                .collect(Collectors.toSet());

        // 요청에 존재하는 파일들
        Set<String> requestImgUrl = Optional.ofNullable(requestDto.imageInfos())
                .orElse(List.of())
                .stream()
                .map(fileUploadRequest -> fileUploadRequest.presignedUrl())
                .collect(Collectors.toSet());

        //삭제 : 기존 이미지 - 요청 이미지
        Set<String> deleteImgUrl = new HashSet<>(registeredImgUrl);
        deleteImgUrl.removeAll(requestImgUrl);
        List<FileEntity> deleteFileEntities = registeredImages.stream()
                .filter(fileEntity -> deleteImgUrl.contains(fileEntity.getUrl()))
                .toList();

        //새롭게 업로드 : 요청 이미지 - 기존 이미지
        Set<String> uploadImgUrl = new HashSet<>(requestImgUrl);
        uploadImgUrl.removeAll(registeredImgUrl);

        //이미지에 대한 중복 체크(url : unique 속성)
        fileService.checkUniqueURL(uploadImgUrl.stream().toList());

        List<FileUploadRequest> requestFiles = new ArrayList<>(Optional.ofNullable(requestDto.imageInfos()).orElse(List.of()));

        List<FileUploadRequest> uploadFiles = requestFiles.stream()
                .filter(fileEntity -> uploadImgUrl.contains(fileEntity.presignedUrl()))
                .toList();

        // 포스터(Festival 도메인에서만 관리)
        String oldPosterUrl = festival.getPosterInfo();
        String newPosterUrl = requestDto.posterInfo().presignedUrl();

        if(!oldPosterUrl.equals(newPosterUrl)){
            temporalFileRepository.deleteById(requestDto.posterInfo().id());
            deleteImgUrl.add(oldPosterUrl);
        }
        festival.updateFestival(requestDto);

        saveFiles(uploadFiles, festival.getId());
        fileService.deleteAllByFileEntity(deleteFileEntities);

        List<String> festivalImgs = fileService.findByDomainIdAndDomainType(festival.getId(), DomainType.FESTIVAL)
                .stream()
                .map(FileEntity::getUrl)
                .toList();

        s3Service.deleteFiles(deleteImgUrl.stream().toList()); // s3에서 삭제
        return new FestivalDetailResponseDto(festival, festivalImgs);
    }

    //축제 상태 변경(admin이 사용자가 등록한 축제를 허용, 관리자)
    @Transactional
    public FestivalListResponse updateState(Long id, FestivalStateDto stateDto) {
        Festival festival = festivalLowService.findFestivalById(id);
        festival.updateState(FestivalState.valueOf(stateDto.state()));
        return new FestivalListResponse(festival);
    }

    //DELETE
    @Transactional
    public void deleteFestivalForManager(Long userId, Long id) {
        Festival festival = checkMyFestival(userId, id);

        deleteRelatedEntity(festival.getId()); // 연관된 엔티티 벌크 쿼리로 모두 삭제

        festivalLowService.deleteById(festival.getId());

        //축제 삭제 시 관련 이미지를 모두 삭제
        fileService.deleteByDomainId(festival.getId(), DomainType.FESTIVAL);
    }

    @Transactional
    public void deleteFestivalForAdmin(Long id) {
        Festival festival = festivalLowService.findFestivalById(id);

        deleteRelatedEntity(festival.getId()); // 연관된 엔티티 벌크 쿼리로 모두 삭제

        festivalLowService.deleteById(festival.getId());

        fileService.deleteByDomainId(festival.getId(), DomainType.FESTIVAL);
    }

    @Transactional
    public void deleteFestivalByManagerId(Long id) {
        List<Long> festivalIds = festivalLowService.findFestivalByManagerId(id)
                .stream().map(Festival::getId).toList();

        festivalLowService.deleteByManagerId(id);

        fileService.deleteByDomainIds(festivalIds, DomainType.REVIEW); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }

    //수정 권한을 확인하기 위한 메서드
    private Festival checkMyFestival(Long userId, Long id) {
        Festival festival = festivalLowService.findFestivalByIdWithManager(id);
        UserEntity manager = festival.getManager();
        if (manager != null && userId.equals(manager.getId())) {
            return festival;
        }
        throw new ForbiddenException(ExceptionCode.FESTIVAL_ACCESS_FORBIDDEN);
    }

    //모든 승인된 축제 검색 기능 - for view
    public List<FestivalListResponse> findApproved() {
        List<Festival> festivalList = festivalLowService.findAllByState(FestivalState.APPROVED);
        return festivalList.stream().map(FestivalListResponse::new).toList();
    }

    private void saveFiles(List<FileUploadRequest> images, Long id) {
        List<FileEntity> files = new ArrayList<>();
        List<Long> temporalFileIds = new ArrayList<>();

        images.forEach(imageInfo -> {
            files.add(new FileEntity(imageInfo.presignedUrl(), FileType.IMAGE, DomainType.FESTIVAL, id));
            temporalFileIds.add(imageInfo.id());
        });

        if (!files.isEmpty()) {
            fileService.saveAll(files);
        }
        temporalFileRepository.deleteByIds(temporalFileIds);
    }

    private void deleteRelatedEntity(Long festivalId) {
        recommendationHistoryLowService.deleteByFestivalId(festivalId);
        wishLowService.deleteByFestivalId(festivalId);
        reviewService.deleteReviewByFestivalId(festivalId);
        chatRoomService.deleteChatRoomByfestivalIdIfExist(festivalId);
    }

}
