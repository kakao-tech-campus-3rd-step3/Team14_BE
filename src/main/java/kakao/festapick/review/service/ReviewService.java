package kakao.festapick.review.service;

import jakarta.validation.Valid;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.exception.DuplicateEntityException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OAuth2UserService oAuth2UserService;
    private final FestivalRepository festivalRepository;
    private final FileService fileService;


    public Long createReview(Long festivalId, @Valid ReviewRequestDto requestDto, String identifier) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
        UserEntity user = oAuth2UserService.findByIdentifier(identifier);

        if (reviewRepository.existsByUserIdAndFestivalId(user.getId(), festivalId)) {
            throw new DuplicateEntityException(ExceptionCode.REVIEW_DUPLICATE);
        }

        Review newReview = new Review(user, festival, requestDto.content(), requestDto.score());
        Review saved = reviewRepository.save(newReview);

        saveFiles(requestDto, saved);

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


    public void updateReview(Long reviewId, @Valid ReviewRequestDto requestDto, String identifier) {
        Review review = reviewRepository.findByUserIdentifierAndId(identifier, reviewId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND));

        review.changeContent(requestDto.content());
        review.changeScore(requestDto.score());

        fileService.deleteByDomainId(reviewId, DomainType.REVIEW);

        saveFiles(requestDto, review);

    }


    public void removeReview(Long reviewId, String identifier) {

        if (reviewRepository.deleteByUserIdentifierAndId(identifier, reviewId) == 0) {
            throw new NotFoundEntityException(ExceptionCode.REVIEW_NOT_FOUND);
        }

        fileService.deleteByDomainId(reviewId, DomainType.REVIEW);
    }

    private void saveFiles(ReviewRequestDto requestDto, Review saved) {
        List<FileEntity> files = new ArrayList<>();

        if(requestDto.imageUrls() != null) requestDto.imageUrls().forEach(url ->
                files.add(new FileEntity(url, FileType.IMAGE, DomainType.REVIEW, saved.getId())));

        if (requestDto.videoUrl() != null && !requestDto.videoUrl().isBlank())
            files.add(new FileEntity(requestDto.videoUrl(), FileType.VIDEO, DomainType.REVIEW, saved.getId()));


        if (!files.isEmpty()) fileService.saveAll(files);
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
