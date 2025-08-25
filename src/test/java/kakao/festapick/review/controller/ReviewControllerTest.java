package kakao.festapick.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.review.domain.Review;
import kakao.festapick.review.dto.ReviewRequestDto;
import kakao.festapick.review.dto.ReviewResponseDto;
import kakao.festapick.review.repository.ReviewRepository;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("리뷰 등록 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void createReviewSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();

        ReviewRequestDto requestDto = new ReviewRequestDto("testtesttest", 3);

        String result =  mockMvc.perform(post(String.format("/api/festivals/%s/reviews", festival.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long savedReviewId = objectMapper.readValue(result, ReviewResponseDto.class).reviewId();

        Optional<Review> find = reviewRepository.findByUserIdentifierAndId(userEntity.getIdentifier(),
                savedReviewId);
        assertThat(find).isPresent();
        Review actual = find.get();
        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival())
                        .isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getUser())
                        .isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getContent())
                        .isEqualTo(requestDto.content()),
                () -> AssertionsForClassTypes.assertThat(actual.getScore())
                        .isEqualTo(requestDto.score())
        );
    }

    @Test
    @DisplayName("리뷰 등록 실패 (없는 축제에 대한 등록)")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void createReviewFail() throws Exception {

        ReviewRequestDto requestDto = new ReviewRequestDto("testtesttest", 3);

        mockMvc.perform(post(String.format("/api/festivals/%s/reviews", 999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 리뷰 조회 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void getReviewsSuccess1() throws Exception {

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("축제 리뷰 조회 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void getReviewsSuccess2() throws Exception {

        Festival festival = saveFestival();

        mockMvc.perform(get(String.format("/api/festivals/%s/reviews", festival.getId())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void removeReviewSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();

        Review target = reviewRepository.save(new Review(userEntity, festival, "test", 3));

        mockMvc.perform(delete(String.format("/api/reviews/%s", target.getId())))
                .andExpect(status().isNoContent());

        Optional<Review> find = reviewRepository.findById(target.getId());
        assertThat(find).isEmpty();
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void updateReviewSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();

        ReviewRequestDto requestDto = new ReviewRequestDto("update", 1);

        Review target = reviewRepository.save(new Review(userEntity, festival, "test", 3));

        mockMvc.perform(patch(String.format("/api/reviews/%s", target.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        Optional<Review> find = reviewRepository.findById(target.getId());

        assertThat(find).isPresent();

        Review actual = find.get();
        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival())
                        .isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getUser())
                        .isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getContent())
                        .isEqualTo(requestDto.content()),
                () -> AssertionsForClassTypes.assertThat(actual.getScore())
                        .isEqualTo(requestDto.score())
        );
    }

    private UserEntity saveUserEntity() {

        return userRepository.save(new UserEntity(identifier,
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE));
    }

    private Festival saveFestival() {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                "11", "test area1", "test area2", "http://asd.example.com/test.jpg", "20250823",
                "20251231");
        Festival festival = new Festival(festivalRequestDto, "http://asd.example.com",
                "testtesttest");

        return festivalRepository.save(festival);
    }
}
