package kakao.festapick.ai.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;

import kakao.festapick.ai.domain.RecommendationForm;
import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.dto.AiRecommendationHistoryResponse;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.dto.FestivalStyle;
import kakao.festapick.ai.dto.RecommendationFormResponse;
import kakao.festapick.ai.repository.RecommendationFormRepository;
import kakao.festapick.ai.repository.RecommendationHistoryRepository;
import kakao.festapick.chat.dto.PreviousMessagesResponseDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestSecurityContextHolderInjection;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AiRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationHistoryRepository recommendationHistoryRepository;

    @Autowired
    private RecommendationFormRepository recommendationFormRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestUtil testUtil;

    @Test
    @DisplayName("이미 추천받은 축제 조회")
    void getRecommendedFestival() throws Exception {

        // given
        UserEntity testUser = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());
        Festival testFestival = festivalRepository.save(testUtil.createTestFestival(testUser));
        RecommendationHistory sveadRecommendationHistory = recommendationHistoryRepository.save(testUtil.createRecommendationHistory(testUser, testFestival));

        AiRecommendationRequest request = new AiRecommendationRequest(
                34,
                List.of(FestivalStyle.FOOD, FestivalStyle.LOCAL, FestivalStyle.TRENDY),
                true,
                false,
                true,
                false,
                null
        );

        RecommendationForm savedRecommendationForm = recommendationFormRepository.save(new RecommendationForm(request, testUser));


        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations/histories")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponseDto<AiRecommendationHistoryResponse> apiResponseDto = objectMapper.readValue(response, new TypeReference<ApiResponseDto<AiRecommendationHistoryResponse>>(){});

        AiRecommendationHistoryResponse content = apiResponseDto.content();

        RecommendationFormResponse recommendationFormResponse = content.recommendationFormResponse();
        List<FestivalListResponse> festivalListResponses = content.recommendedFestivals();


        assertSoftly(softly -> {
            softly.assertThat(festivalListResponses).hasSize(1);
            softly.assertThat(recommendationFormResponse.areaCode()).isEqualTo(savedRecommendationForm.getAreaCode());
        });
    }

    @Test
    @DisplayName("추천받았던적이 없다면 빈 리스트 반환")
    void getRecommendedFestivalEmpty() throws Exception {

        // given
        UserEntity testUser = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations/histories")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponseDto<AiRecommendationHistoryResponse> apiResponseDto = objectMapper.readValue(response, new TypeReference<ApiResponseDto<AiRecommendationHistoryResponse>>(){});

        AiRecommendationHistoryResponse content = apiResponseDto.content();

        RecommendationFormResponse recommendationFormResponse = content.recommendationFormResponse();
        List<FestivalListResponse> festivalListResponses = content.recommendedFestivals();


        assertSoftly(softly -> {
            softly.assertThat(festivalListResponses).hasSize(0);
            softly.assertThat(recommendationFormResponse).isNull();
        });
    }
}
