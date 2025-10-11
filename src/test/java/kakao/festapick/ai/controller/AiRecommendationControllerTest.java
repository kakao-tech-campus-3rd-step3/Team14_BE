package kakao.festapick.ai.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.ai.repository.RecommendationHistoryRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.repository.FestivalRepository;
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
        RecommendationHistory saved = recommendationHistoryRepository.save(testUtil.createRecommendationHistory(testUser, testFestival));


        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations/histories")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode node = objectMapper.readTree(response);
        List<FestivalListResponse> festivalListResponses =
                objectMapper.convertValue(node.get("content"),
                        new TypeReference<List<FestivalListResponse>>() {});


        FestivalListResponse festivalListResponse = festivalListResponses.get(0);

        System.out.println(festivalListResponse.toString());

        assertSoftly(softly -> {
            softly.assertThat(festivalListResponse.id()).isEqualTo(testFestival.getId());
            softly.assertThat(festivalListResponse.posterInfo()).isEqualTo(testFestival.getPosterInfo());

        });

    }
}
