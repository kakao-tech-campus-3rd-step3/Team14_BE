package kakao.festapick.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.domain.TemporalFile;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestSecurityContextHolderInjection;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerTest {

    private static final String identifier = "GOOGLE_1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private TemporalFileRepository temporalFileRepository;

    private final TestUtil testUtil = new TestUtil();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReviewSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();
        TemporalFile t1 = temporalFileRepository.save(new TemporalFile("imageUrl1"));
        TemporalFile t2 = temporalFileRepository.save(new TemporalFile("imageUrl2"));
        TemporalFile t3 = temporalFileRepository.save(new TemporalFile("videoUrl"));


        ReviewRequestDto requestDto = new ReviewRequestDto("testtesttest", 3,
                List.of(new FileUploadRequest(t1),new FileUploadRequest(t2)),
                new FileUploadRequest(t3));

        String header = mockMvc.perform(post(String.format("/api/festivals/%s/reviews", festival.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("location");

        int idx = header.lastIndexOf("/");
        Long savedReviewId = Long.valueOf(header.substring(idx + 1));

        Optional<Review> find = reviewRepository.findByUserIdAndId(userEntity.getId(),
                savedReviewId);
        assertThat(find).isPresent();
        Review actual = find.get();
        assertSoftly(softly -> {
                softly.assertThat(actual.getId()).isNotNull();
                softly.assertThat(actual.getFestival().getId()).isEqualTo(festival.getId());
                softly.assertThat(actual.getUser().getId()).isEqualTo(userEntity.getId());
                softly.assertThat(actual.getContent()).isEqualTo(requestDto.content());
                softly.assertThat(actual.getScore()).isEqualTo(requestDto.score());
            }
        );

        List<FileEntity> files = fileService.findByDomainIdAndDomainType(actual.getId(), DomainType.REVIEW);
        List<TemporalFile> all = temporalFileRepository.findAll();

        assertThat(files.size()).isEqualTo(3);
        assertThat(all.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("리뷰 등록 실패 (없는 축제에 대한 등록)")
    void createReviewFail() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        ReviewRequestDto requestDto = new ReviewRequestDto("testtesttest", 3, null, null);

        mockMvc.perform(post(String.format("/api/festivals/%s/reviews", 999L))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 리뷰 조회 성공")
    void getReviewsSuccess1() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        mockMvc.perform(get("/api/reviews/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("축제 리뷰 조회 성공")
    void getReviewsSuccess2() throws Exception {

        Festival festival = saveFestival();
        UserEntity testUser = userRepository.save(testUtil.createTestUser());
        Review review = reviewRepository.save(testUtil.createReview(testUser, festival));

        String response = mockMvc.perform(get(String.format("/api/festivals/%s/reviews", festival.getId())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Map<String, Object> mapResponse = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
        });

        List<ReviewResponseDto> content = objectMapper.convertValue(mapResponse.get("content"), new TypeReference<List<ReviewResponseDto>>() {});

        ReviewResponseDto reviewResponseDto = content.get(0);

        assertSoftly(softly -> {
            softly.assertThat(reviewResponseDto.reviewId()).isEqualTo(review.getId());
            softly.assertThat(reviewResponseDto.userId()).isEqualTo(testUser.getId());
            softly.assertThat(reviewResponseDto.content()).isEqualTo(review.getContent());
            softly.assertThat(reviewResponseDto.score()).isEqualTo(review.getScore());
            softly.assertThat(reviewResponseDto.festivalTitle()).isEqualTo(review.getFestival().getTitle());
        });

    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void removeReviewSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();

        Review target = reviewRepository.save(new Review(userEntity, festival, "test 정성리뷰 10글자 이상 해야 해요", 3));

        mockMvc.perform(delete(String.format("/api/reviews/%s", target.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNoContent());

        Optional<Review> find = reviewRepository.findById(target.getId());
        assertThat(find).isEmpty();
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReviewSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();

        TemporalFile t2 = temporalFileRepository.save(new TemporalFile("imageUrl2"));
        TemporalFile t3 = temporalFileRepository.save(new TemporalFile("videoUrl"));

        ReviewRequestDto requestDto = new ReviewRequestDto
                ("update 정성리뷰 10글자 이상 해야 해요", 1,
                        List.of(new FileUploadRequest(1L, "imageUrl1"),new FileUploadRequest(t2)),
                        new FileUploadRequest(t3));

        Review target = reviewRepository.save(new Review(userEntity, festival, "test 정성리뷰 10글자 이상 해야 해요", 3));

        fileService.saveAll(List.of(new FileEntity("oldImageUrl", FileType.IMAGE, DomainType.REVIEW, target.getId()),
                new FileEntity("imageUrl1", FileType.VIDEO, DomainType.REVIEW, target.getId())));

        mockMvc.perform(put(String.format("/api/reviews/%s", target.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk());

        Optional<Review> find = reviewRepository.findById(target.getId());

        assertThat(find).isPresent();

        Review actual = find.get();
        assertSoftly(softly -> {
                    softly.assertThat(actual.getId()).isNotNull();
                    softly.assertThat(actual.getFestival().getId()).isEqualTo(festival.getId());
                    softly.assertThat(actual.getUser().getId()).isEqualTo(userEntity.getId());
                    softly.assertThat(actual.getContent()).isEqualTo(requestDto.content());
                    softly.assertThat(actual.getScore()).isEqualTo(requestDto.score());
                }
        );

        List<FileEntity> files = fileService.findByDomainIdAndDomainType(actual.getId(), DomainType.REVIEW);
        List<TemporalFile> all = temporalFileRepository.findAll();
        assertThat(files.size()).isEqualTo(3);
        assertThat(all.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("특정 리뷰 조회 성공")
    void getReviewByIdSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();


        Review target = reviewRepository.save(
                new Review(userEntity, festival, "test 정성리뷰 10글자 이상 해야 해요", 3));

        String response = mockMvc.perform(get(String.format("/api/reviews/%s", target.getId())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponseDto<ReviewResponseDto> result = objectMapper.readValue(response, new TypeReference<ApiResponseDto<ReviewResponseDto>>() {});
        ReviewResponseDto content = result.content();

        assertSoftly(softly-> {
            softly.assertThat(content.reviewId()).isEqualTo(target.getId());
            softly.assertThat(content.content()).isEqualTo(target.getContent());
            softly.assertThat(content.score()).isEqualTo(target.getScore());
            softly.assertThat(content.reviewerName()).isEqualTo(target.getReviewerName());
            softly.assertThat(content.festivalTitle()).isEqualTo(target.getFestivalTitle());
            softly.assertThat(content.videoUrl()).isNull();
            softly.assertThat(content.imageUrls()).isNull();
        });
    }

    private UserEntity saveUserEntity() {

        return userRepository.save(testUtil.createTestUser(identifier));
    }

    private Festival saveFestival() throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg", testUtil.toLocalDate("20250823"),
                testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, testUtil.createTourDetailResponse());

        return festivalRepository.save(festival);
    }

}
