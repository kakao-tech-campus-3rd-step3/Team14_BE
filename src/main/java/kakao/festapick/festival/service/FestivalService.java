package kakao.festapick.festival.service;

import kakao.festapick.ai.service.RecommendationHistoryLowService;
import kakao.festapick.chat.service.ChatRoomService;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.*;
import kakao.festapick.festivalnotice.service.FestivalNoticeService;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.FileUploadHelper;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
import kakao.festapick.permission.festivalpermission.service.FestivalPermissionService;
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
    private final FestivalPermissionService festivalPermissionService;
    private final FestivalCacheService festivalCacheService;
    private final FestivalNoticeService festivalNoticeService;
    private final FileUploadHelper fileUploadHelper;

    //CREATE
    @Transactional
    public Long addCustomizedFestival(FestivalCustomRequestDto requestDto, Long userId) {
        UserEntity user = userLowService.getReferenceById(userId);

        Festival festival = new Festival(requestDto, user);
        Festival savedFestival = festivalLowService.save(festival);

        //temp에서 포스터 삭제
        temporalFileRepository.deleteById(requestDto.posterInfo().id());

        //관련 이미지 업로드(포스터의 경우에는 festival 도메인에서만 관리)
        if(requestDto.imageInfos() != null && !(requestDto.imageInfos().isEmpty())){
            fileUploadHelper.saveFiles(requestDto.imageInfos(), savedFestival.getId(), FileType.IMAGE, DomainType.FESTIVAL);
        }

        return savedFestival.getId();
    }

    //Id를 통한 축제 조회
    public FestivalDetailResponseDto findOneById(Long festivalId, Long userId) {
        Festival festival = festivalLowService.findByIdWithReviews(festivalId);
        List<String> images = fileService.findByDomainIdAndDomainType(festivalId, DomainType.FESTIVAL)
                .stream()
                .map(FileEntity::getUrl)
                .toList();

        Double averageScore = festivalCacheService.calculateReviewScore(festival);

        // 좋아요 개수 반환
        long wishCount = festivalCacheService.getWishCount(festival);
        // userId가 null 이라면 인증되지 않은 사용자이기 때문에 항상 isMyWish가 항상 false
        boolean isMyWish = festivalCacheService.checkIsMyWish(userId, festival);

        return new FestivalDetailResponseDto(festival, images, averageScore, wishCount, isMyWish);
    }


    //내가 관리자인 축제를 조회
    public Page<FestivalListResponse> findMyFestivals(Long userId, Pageable pageable){

        return festivalLowService.findFestivalByManagerId(userId, pageable)
                .map(this::getFestivalListResponse);
    }

    //내가 등록한 축제를 조회
    public Page<FestivalCustomListResponse> findMyCustomFestivals(Long userId, Pageable pageable){
        return festivalLowService.findCustomFestivalByManagerId(userId, pageable)
                .map(FestivalCustomListResponse::new);
    }

    //지역코드와 날짜(오늘)를 통해 승인된 축제를 조회
    public Page<FestivalListResponse> findApprovedAreaAndDate(int areaCode, Pageable pageable) {

        //전국이면 null(조건 없음)
        Integer areaCodeSearch = areaCode == 0 ? null : areaCode;
        Page<Festival> festivalList = festivalLowService.findFestivalByAreaCodeAndDate(areaCodeSearch, LocalDate.now(), pageable);

        return festivalList.map(this::getFestivalListResponse);
    }

    public Page<FestivalListResponse> findFestivalByTitle(String keyWord, Pageable pageable){
        Page<Festival> festivals = festivalLowService.findFestivalByTitle(keyWord, FestivalState.APPROVED, pageable);
        return festivals.map(this::getFestivalListResponse);
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

        // 포스터(Festival 도메인에서만 관리)
        String oldPosterUrl = festival.getPosterInfo();
        String newPosterUrl = requestDto.posterInfo().presignedUrl();

        if(!oldPosterUrl.equals(newPosterUrl)){
            temporalFileRepository.deleteById(requestDto.posterInfo().id());
            s3Service.deleteS3File(oldPosterUrl); // s3에서 삭제 - 포스터
        }

        festival.updateFestival(requestDto);

        if(requestDto.imageInfos() != null){
            fileUploadHelper.updateFiles(id, DomainType.FESTIVAL, FileType.IMAGE, requestDto.imageInfos());
        }

        List<String> festivalImgs = fileService.findByDomainIdAndDomainType(festival.getId(), DomainType.FESTIVAL)
                .stream()
                .map(FileEntity::getUrl)
                .toList();



        Double averageScore = festivalCacheService.calculateReviewScore(festival);
        long wishCount = festivalCacheService.getWishCount(festival);

        boolean isMyWish = festivalCacheService.checkIsMyWish(userId, festival);

        return new FestivalDetailResponseDto(festival, festivalImgs, averageScore, wishCount, isMyWish);
    }

    //축제 상태 변경(admin이 사용자가 등록한 축제를 허용, 관리자)
    @Transactional
    public FestivalListResponse updateState(Long id, FestivalStateDto stateDto) {
        Festival festival = festivalLowService.findByIdWithReviews(id);
        festival.updateState(FestivalState.valueOf(stateDto.state()));

        return getFestivalListResponse(festival);
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

        festivalIds.forEach(festivalId -> deleteRelatedEntity(festivalId));

        festivalLowService.deleteByManagerId(id);

        fileService.deleteByDomainIds(festivalIds, DomainType.FESTIVAL); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }

    //FestivalManager 박탈 시,
    @Transactional
    public void deleteCustomFestivalByUserId(Long userId) {

        List<Long> customFestivals = festivalLowService.findCustomFestivalByManagerId(userId)
                .stream()
                .map(festival -> festival.getId())
                .toList();

        customFestivals.forEach(
                festivalId -> {
                    deleteRelatedEntity(festivalId); // 연관된 엔티티 벌크 쿼리로 모두 삭제

                    festivalLowService.deleteById(festivalId);

                    //축제 삭제 시 관련 이미지를 모두 삭제
                    fileService.deleteByDomainId(festivalId, DomainType.FESTIVAL);
                }
        );

    }

    //수정 권한을 확인하기 위한 메서드
    private Festival checkMyFestival(Long userId, Long id) {
        Festival festival = festivalLowService.findByIdWithReviews(id);
        UserEntity manager = festival.getManager();
        if (manager != null && userId.equals(manager.getId())) {
            return festival;
        }
        throw new ForbiddenException(ExceptionCode.FESTIVAL_ACCESS_FORBIDDEN);
    }

    //모든 승인된 축제 검색 기능 - for view
    public List<FestivalListResponse> findApproved() {
        List<Festival> festivalList = festivalLowService.findAllByState(FestivalState.APPROVED);
        return festivalList.stream().map(this::getFestivalListResponse).toList();
    }

    private FestivalListResponse getFestivalListResponse(Festival festival) {
        Double averageScore = festivalCacheService.calculateReviewScore(festival);
        long wishCount = festivalCacheService.getWishCount(festival);
        return new FestivalListResponse(festival, averageScore, wishCount);
    }

    private void deleteRelatedEntity(Long festivalId) {
        recommendationHistoryLowService.deleteByFestivalId(festivalId);
        wishLowService.deleteByFestivalId(festivalId);
        reviewService.deleteReviewByFestivalId(festivalId);
        chatRoomService.deleteChatRoomByfestivalIdIfExist(festivalId);
        festivalPermissionService.deleteFestivalPermissionByFestivalId(festivalId);
        festivalNoticeService.deleteByFestivalId(festivalId);
    }

}
