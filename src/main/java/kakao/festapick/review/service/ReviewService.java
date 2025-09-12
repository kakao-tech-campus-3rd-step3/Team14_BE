package kakao.festapick.review.service;

import jakarta.validation.Valid;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final FestivalRepository festivalRepository;
    private final FileService fileService;
    private final TemporalFileRepository temporalFileRepository;
    private final S3Service s3Service;


    public Long createReview(Long festivalId, ReviewRequestDto requestDto, String identifier) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        UserEntity user = userService.findByIdentifier(identifier);

        if (reviewRepository.existsByUserIdAndFestivalId(user.getId(), festivalId)) {
            throw new DuplicateEntityException(ExceptionCode.REVIEW_DUPLICATE);
        }

        Review newReview = new Review(user, festival, requestDto.content(), requestDto.score());
        Review saved = reviewRepository.save(newReview);

        saveFiles(requestDto.imageInfos(), requestDto.videoInfo(), saved);

        return saved.getId();
    }

    public Page<ReviewResponseDto> getFestivalReviews(Long festivalId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByFestivalIdWithAll(festivalId, pageable);

        List<FileEntity> files = findFilesByReview(reviews);

        HashMap<Long, List<String>> imageUrls = new HashMap<>();
        HashMap<Long, String> videoUrl = new HashMap<>();

        reviewIdAndUrlMapping(files, imageUrls, videoUrl);


        return reviews.map(review->
                new ReviewResponseDto(review, imageUrls.getOrDefault(review.getId(), List.of()), videoUrl.get(review.getId()))
        );
    }

    public ReviewResponseDto getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));

        List<FileEntity> files = fileService.findByDomainIdAndDomainType(reviewId, DomainType.REVIEW);

        HashMap<Long, List<String>> imageUrls = new HashMap<>();
        HashMap<Long, String> videoUrl = new HashMap<>();

        reviewIdAndUrlMapping(files, imageUrls, videoUrl);

        return new ReviewResponseDto(review, imageUrls.get(reviewId), videoUrl.get(reviewId));

    }

    public Page<ReviewResponseDto> getMyReviews(String identifier, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserIdentifierWithAll(identifier, pageable);


        List<FileEntity> files = findFilesByReview(reviews);

        HashMap<Long, List<String>> imageUrls = new HashMap<>();
        HashMap<Long, String> videoUrl = new HashMap<>();

        reviewIdAndUrlMapping(files, imageUrls, videoUrl);


        return reviews.map(review -> new ReviewResponseDto(review, imageUrls.get(review.getId()), videoUrl.get(review.getId())));
    }


    /**
     * 1. ReviewRequestDto의 사진들은 기존에 존재했던 사진들도 들어온다. (PUT 메소드이기 때문)
     * 2. 기존 리뷰에 존재했던 파일들과 ReviewRequestDto의 요청 파일들을 비교한다.
     * 3. 기존 리뷰에는 존재하는 파일이지만 ReviewRequestDto에 없다면 삭제
     * 4. 기존 리뷰에 존재하지 않는 파일이지만 ReviewRequestDto에 존재한다면 추가를 해야한다.
     */

    public void updateReview(Long reviewId, @Valid ReviewRequestDto requestDto, String identifier) {
        Review review = reviewRepository.findByUserIdentifierAndId(identifier, reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));

        review.changeContent(requestDto.content());
        review.changeScore(requestDto.score());

        // 기존 리뷰 파일
        List<FileEntity> existingFiles = fileService.findByDomainIdAndDomainType(reviewId, DomainType.REVIEW);
        Set<String> existingUrls = existingFiles.stream()
                .map(FileEntity::getUrl)
                .collect(Collectors.toSet());

        // 요청에서 들어온 URL들
        Set<String> requestImageUrls = new HashSet<>();
        String requestVideoUrl = requestDto.videoInfo() != null ?requestDto.videoInfo().presignedUrl() : null;

        if (requestDto.imageInfos() != null)
            requestImageUrls.addAll(
                    requestDto.imageInfos().stream().map(FileUploadRequest::presignedUrl).collect(Collectors.toSet()));


        // 삭제할 파일 : DB에는 있는데 요청에는 없는 것
        List<FileEntity> oldFiles = existingFiles.stream()
                .filter(fileEntity -> {
                    String url = fileEntity.getUrl();
                    return !(requestImageUrls.contains(url) || Objects.equals(url,requestVideoUrl));
                })
                .toList();

        // 새로 추가할 이미지 : 요청에는 있는데 DB에는 없는 것
        List<FileUploadRequest> newImages = requestDto.imageInfos() == null ? List.of()
                : requestDto.imageInfos().stream()
                .filter(file -> !existingUrls.contains(file.presignedUrl()))
                .toList();

        // 새로 추가할 비디오 = 요청에는 있는데 DB에는 없는 것
        FileUploadRequest newVideo = null;
        if (requestDto.videoInfo() != null && !existingUrls.contains(requestDto.videoInfo().presignedUrl())) {
            newVideo = requestDto.videoInfo();
        }


        saveFiles(newImages, newVideo, review);   // 새로운 파일만 저장
        fileService.deleteAllByFileEntity(oldFiles); // 오래된 파일은 삭제
        s3Service.deleteFiles(oldFiles.stream().map(FileEntity::getUrl).toList()); // s3 파일 삭제는 항상 마지막에 호출
    }


    public void removeReview(Long reviewId, String identifier) {

        if (reviewRepository.deleteByUserIdentifierAndId(identifier, reviewId) == 0) {
            throw new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND);
        }

        fileService.deleteByDomainId(reviewId, DomainType.REVIEW);
    }

    //review의 id만 넘기는건 어떤지?
    private void saveFiles(List<FileUploadRequest> imageInfos, FileUploadRequest videoInfo, Review saved) {
        List<FileEntity> files = new ArrayList<>();
        List<Long> temporalFileIds = new ArrayList<>();

        if(imageInfos != null)
            imageInfos.forEach(imageInfo ->{
                files.add(new FileEntity(imageInfo.presignedUrl(), FileType.IMAGE, DomainType.REVIEW, saved.getId()));
                temporalFileIds.add(imageInfo.id());
            });

        if (videoInfo != null){
            files.add(new FileEntity(videoInfo.presignedUrl(), FileType.VIDEO, DomainType.REVIEW, saved.getId()));
            temporalFileIds.add(videoInfo.id());
        }


        if (!files.isEmpty()) fileService.saveAll(files);
        temporalFileRepository.deleteByIds(temporalFileIds);
    }

    // 리뷰와 관련된 파일 가져오기 - 쿼리 1개
    private List<FileEntity> findFilesByReview(Page<Review> reviews) {
        List<Long> domainIds = reviews.stream()
                .map(Review::getId)
                .toList();

        return fileService.findAllFileEntityByDomain(domainIds, DomainType.REVIEW);
    }

    // reviewId와 url을 매핑
    private static void reviewIdAndUrlMapping(List<FileEntity> files, HashMap<Long, List<String>> imageUrls, HashMap<Long, String> videoUrl) {
        files.forEach(file-> {
            if (file.getFileType().equals(FileType.IMAGE)) {
                if (!imageUrls.containsKey(file.getDomainId())) imageUrls.put(file.getDomainId(), new ArrayList<>());
                imageUrls.get(file.getDomainId()).add(file.getUrl());
            }
            else {
                videoUrl.put(file.getDomainId(), file.getUrl());
            }
        });
    }
}
