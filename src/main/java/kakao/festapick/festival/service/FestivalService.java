package kakao.festapick.festival.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.dto.FestivalListResponseForAdmin;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import kakao.festapick.festival.dto.FestivalStateDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
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
        
        temporalFileRepository.deleteById(requestDto.posterInfo().id());

        //관련 이미지 업로드(포스터의 경우에는 festival 도메인에서만 관리)
        if(requestDto.imageInfos() != null){
            saveFiles(requestDto.imageInfos(), savedFestival.getId());
        }

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
    public Page<FestivalListResponse> findMyFestivals(String identifier, Pageable pageable){
        UserEntity user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
        return festivalRepository.findFestivalByManagerId(user.getId(), pageable)
                .map(FestivalListResponse::new);
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
    //축제 정보를 업데이트(축제 관리자)
    @Transactional
    public FestivalDetailResponseDto updateFestival(String identifier, Long id, FestivalUpdateRequestDto requestDto) {

        Festival festival = checkMyFestival(identifier, id);

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

        List<FileUploadRequest> requestFiles = new ArrayList<>(
                Optional.ofNullable(requestDto.imageInfos()).orElse(List.of())
        );

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

        //축제 삭제 시 관련 이미지를 모두 삭제
        fileService.deleteByDomainId(festival.getId(), DomainType.FESTIVAL);
    }

    @Transactional
    public void deleteFestivalForAdmin(Long id) {
        Festival festival = festivalRepository.findFestivalById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        festivalRepository.deleteById(festival.getId());

        fileService.deleteByDomainId(festival.getId(), DomainType.FESTIVAL);
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

}
