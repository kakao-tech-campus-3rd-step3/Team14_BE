package kakao.festapick.festival.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.dto.FestivalListResponseForAdmin;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import kakao.festapick.festival.dto.FestivalStateDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.repository.QFestivalRepository;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final QFestivalRepository qFestivalRepository;
    private final S3Service s3Service;
    private final TemporalFileRepository temporalFileRepository;
    private final FileService fileService;

    //CREATE
    @Transactional
    public Long addCustomizedFestival(FestivalCustomRequestDto requestDto, String identifier) {
        UserEntity user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
        Festival festival = new Festival(requestDto, user);
        Festival savedFestival = festivalRepository.save(festival);

        //포스터 및 관련 이미지 업로드
        saveFiles(requestDto.posterInfo(), requestDto.imageInfos(), savedFestival.getId());
        return savedFestival.getId();
    }

    //create - TourAPI
    @Transactional
    public Long addFestival(FestivalRequestDto requestDto, TourDetailResponse detailResponse) {
        Festival festival = new Festival(requestDto, detailResponse);
        Festival savedFestival = festivalRepository.save(festival);
        return savedFestival.getId();
    }

    //Id를 통한 축제 조회
    public FestivalDetailResponseDto findOneById(Long festivalId) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        List<String> images = fileService.findByDomainIdAndDomainType(festivalId, DomainType.FESTIVAL)
                .stream()
                .map(fileEntity -> fileEntity.getUrl())
                .toList();
        return new FestivalDetailResponseDto(festival, images);
    }

    //내가 등록한 축제를 조회
    public List<FestivalListResponse> findMyFestivals(String identifier){
        UserEntity user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
        return festivalRepository.findFestivalByManagerId(user.getId())
                .stream()
                .map(festival -> new FestivalListResponse(
                        festival.getId(),
                        festival.getTitle(),
                        festival.getAddr1(),
                        festival.getAddr2(),
                        festival.getPosterInfo(),
                        festival.getStartDate(),
                        festival.getEndDate())
                )
                .toList();
    }

    //지역코드와 날짜(오늘)를 통해 승인된 축제를 조회
    public Page<FestivalListResponse> findApprovedAreaAndDate(int areaCode, Pageable pageable) {
        Page<Festival> festivalList = festivalRepository.findFestivalByAreaCodeAndDate(areaCode,
                LocalDate.now(), FestivalState.APPROVED, pageable);
        return festivalList.map(FestivalListResponse::new);
    }

    //모든 축제 검색 기능(관리자)
    public Page<FestivalListResponseForAdmin> findAllWithPage(FestivalSearchCondForAdmin cond,
            Pageable pageable) {
        return qFestivalRepository.findByStateAndTitleLike(cond, pageable)
                .map(FestivalListResponseForAdmin::new);
    }

    //UPDATE
    //축제 정보를 업데이트(관리자)
    @Transactional
    public FestivalDetailResponseDto updateFestival(String identifier, Long id,
            FestivalUpdateRequestDto requestDto) {
        Festival festival = checkMyFestival(identifier, id);
        String oldImageUrl = festival.getPosterInfo();

        festival.updateFestival(requestDto);
        if (requestDto.posterInfo() != null) {
            temporalFileRepository.deleteById(requestDto.posterInfo().id());
        }

        s3Service.deleteS3File(oldImageUrl); // s3 파일 삭제는 항상 마지막에 호출

        return new FestivalDetailResponseDto(festival);
    }

    //축제 상태 변경(admin이 사용자가 등록한 축제를 허용, 관리자)
    @Transactional
    public FestivalListResponse updateState(Long id, FestivalStateDto stateDto) {
        Festival festival = festivalRepository.findFestivalById(id).orElseThrow(
                () -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND)
        );
        festival.updateState(FestivalState.valueOf(stateDto.state()));
        return new FestivalListResponse(festival);
    }

    //DELETE
    @Transactional
    public void removeOne(String identifier, Long id) {
        Festival festival = checkMyFestival(identifier, id);
        festivalRepository.deleteById(festival.getId());

        s3Service.deleteS3File(festival.getPosterInfo()); // s3 파일 삭제는 항상 마지막에 호출
    }

    @Transactional
    public void deleteFestivalForAdmin(Long festivalId) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));

        festivalRepository.deleteById(festivalId);

        // 꼭 S3 파일 삭제는 외부 호출이기 때문에마지막에 호출해야함!
        s3Service.deleteS3File(festival.getPosterInfo());
    }

    //수정 권한을 확인하기 위한 메서드
    private Festival checkMyFestival(String identifier, Long id) {
        Festival festival = festivalRepository.findFestivalByIdWithManager(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        UserEntity manager = festival.getManager();
        if (manager != null && identifier.equals(manager.getIdentifier())) {
            return festival;
        }
        throw new ForbiddenException(ExceptionCode.FESTIVAL_ACCESS_FORBIDDEN);
    }

    //모든 승인된 축제 검색 기능 - for view
    public List<FestivalListResponse> findApproved() {
        List<Festival> festivalList = festivalRepository.findAllByState(FestivalState.APPROVED);
        return festivalList.stream().map(FestivalListResponse::new).toList();
    }

    private void saveFiles(FileUploadRequest posterInfo, List<FileUploadRequest> imageInfos, Long id) {
        List<FileEntity> files = new ArrayList<>();
        List<Long> temporalFileIds = new ArrayList<>();

        //포스터의 경우에는 필수 등록임
        files.add(new FileEntity(posterInfo.presignedUrl(), FileType.IMAGE, DomainType.FESTIVAL, id));

        //관련 이미지의 경우 필수 사항 x
        if (imageInfos != null) {
            imageInfos.forEach(imageInfo -> {
                files.add(new FileEntity(imageInfo.presignedUrl(), FileType.IMAGE, DomainType.FESTIVAL, id));
                temporalFileIds.add(imageInfo.id());
            });
        }

        if (!files.isEmpty()) {
            fileService.saveAll(files);
        }
        temporalFileRepository.deleteByIds(temporalFileIds);
    }

}
