package kakao.festapick.festival.controller;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import kakao.festapick.dto.ApiResponseDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FestivalUserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private FestivalRepository festivalRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private TestUtil testUtil;

    @BeforeEach
    void initTestDB() throws Exception {
        String identifier = "KAKAO_141036";
        UserEntity user = testUtil.createTestManager(identifier);
        userRepository.save(user);

        Festival festival1 = createFestival("FESTAPICK_001" , "부산대축제", 1, testUtil.toLocalDate("20250810"), testUtil.toLocalDate("20250820"));
        festivalRepository.save(festival1);

        Festival festival2 = createFestival("FESTAPICK_002", "경북대축제", 2, testUtil.toLocalDate("20250812"), testUtil.toLocalDate("20250815"));
        festivalRepository.save(festival2);

        Festival festival3 = createFestival("FESTAPICK_003","정컴인축제",  1, testUtil.toLocalDate("20250814"), testUtil.toLocalDate("20250817"));
        festivalRepository.save(festival3);

        Festival festival4 = creatCustomFestival("의생공축제",  1, testUtil.toLocalDate("20250817"), testUtil.toLocalDate("20250818"), user);
        festivalRepository.save(festival4);

        Festival festival5 = creatCustomFestival("밀양대축제", 3, testUtil.toLocalDate("20250821"), testUtil.toLocalDate("20250823"), user);
        festivalRepository.save(festival5);
    }

    @Test
    @WithCustomMockUser(identifier = "KAKAO_141036", role = "ROLE_FESTIVAL_MANAGER")
    @DisplayName("축제 등록 - FESTIVAL_MANAGER")
    void addFestival() throws Exception {

        //given
        FestivalCustomRequestDto requestDto = customFestivalRequest("title", 1, testUtil.toLocalDate("20250804"), testUtil.toLocalDate("20250806"));
        String customRequestDto = objectMapper.writeValueAsString(requestDto);

        //when-then
        mockMvc.perform(post("/api/festivals")
                        .content(customRequestDto)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }


    @Test
    @WithMockUser
    @DisplayName("축제 등록 실패 - USER")
    void addFestivalFail() throws Exception {

        //given
        FestivalCustomRequestDto requestDto = customFestivalRequest("title", 1, testUtil.toLocalDate("20250804"), testUtil.toLocalDate("20250806"));
        String customRequestDto = objectMapper.writeValueAsString(requestDto);

        //when-then
        mockMvc.perform(post("/api/festivals")
                        .content(customRequestDto)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }


    @Test
    @DisplayName("현재 특정 지역에서 진행중인 모든 축제를 조회")
    void getCurrentFestivalByArea() throws Exception {
        //given
        int areaCode = 1; //1번 지역에 3개의 축제가 열리지만 승인된건 2개뿐임,,

        Festival festival1 = createFestival("FESTAPICK_999", "정컴 모여라", 1, testUtil.toLocalDate("20250801"), LocalDate.now());
        festivalRepository.save(festival1);

        Festival festival2 = createFestival("FESTAPICK_998", "부산대 모여라", 1, testUtil.toLocalDate("20240801"), testUtil.toLocalDate("20250815"));
        festivalRepository.save(festival2);

        Festival festival3 = createFestival("FESTAPICK_997", "양산캠 모여라", 1, testUtil.toLocalDate("20250801"), LocalDate.now());
        festivalRepository.save(festival3);

        //when-then
        MvcResult mvcResult = mockMvc.perform(get("/api/festivals/area/{areaCode}", areaCode))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> festivals = read(response, "$.content");
        assertThat(festivals.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("축제의 상세 정보 조회")
    void getFestivalInfo() throws Exception{
        //given
        Festival festival = createFestival("FESTAPICK_999", "정컴 컴공 다모여라", 1, testUtil.toLocalDate("20250901"), testUtil.toLocalDate("20250903"));
        Long festivalId = festivalRepository.save(festival).getId();

        //when-then
        MvcResult result = mockMvc.perform(get("/api/festivals/{festvialID}", festivalId))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        ApiResponseDto<FestivalDetailResponseDto> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponseDto<FestivalDetailResponseDto>>() {});
        FestivalDetailResponseDto content = response.content();
        assertAll(
                () -> assertThat(content.title()).isEqualTo(festival.getTitle()),
                () -> assertThat(content.overView()).isNotNull(),
                () -> assertThat(content.addr2()).isNotNull()
        );

    }

    @Test
    @WithCustomMockUser(identifier = "KAKAO_123456", role = "ROLE_FESTIVAL_MANAGER")
    void updateFestivalInfo() throws Exception {
        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);

        Festival festival = creatCustomFestival("축제1", 1, testUtil.toLocalDate("20250803"),
                testUtil.toLocalDate("20250805"), user);
        Festival saved = festivalRepository.save(festival);

        FestivalUpdateRequestDto updateInfo = createUpdateInfo("카테캠 축제 시즌 3");
        String updateRequest = objectMapper.writeValueAsString(updateInfo);

        //when-then
        MvcResult mvcResult = mockMvc.perform(patch("/api/festivals/{festivalId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        ApiResponseDto<FestivalDetailResponseDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<ApiResponseDto<FestivalDetailResponseDto>>() {});
        FestivalDetailResponseDto content = response.content();

        assertAll(
                () -> assertThat(content.id()).isEqualTo(saved.getId()),
                () -> assertThat(content.title()).isEqualTo("카테캠 축제 시즌 3")
        );
    }

    @Test
    @WithCustomMockUser(identifier = "KAKAO_123456", role = "ROLE_FESTIVAL_MANAGER")
    void removeFestival() throws Exception {
        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);

        Festival festival = creatCustomFestival("축제1", 1, testUtil.toLocalDate("20250803"), testUtil.toLocalDate("20250805"), user);
        Festival saved = festivalRepository.save(festival);

        //when-then
        mockMvc.perform(delete("/api/festivals/{festivalId}", saved.getId()))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    @WithMockUser
    @DisplayName("User가 축제를 삭제하려고 하는 경우 - 삭제 실패")
    void removeFestivalFail() throws Exception {
        //given
        Festival festival = createFestival("FESTAPICK_999", "카테캠축제", 1, testUtil.toLocalDate("20250901"), testUtil.toLocalDate("20250903"));
        Festival saved = festivalRepository.save(festival);

        //when-then
        mockMvc.perform(delete("/api/festivals/{festivalId}", saved.getId()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private FestivalRequestDto FestivalRequest(String contentId, String title, int areaCode, LocalDate startDate, LocalDate endDate){
        return new FestivalRequestDto(
                contentId, title, areaCode, "addr1", "addr2",
                "imageUrl", startDate, endDate
        );
    }

    private Festival createFestival(String contentId, String title, int areaCode, LocalDate startDate, LocalDate endDate)
            throws Exception {
        return new Festival(FestivalRequest(contentId, title, areaCode, startDate, endDate), testUtil.createTourDetailResponse());
    }

    private FestivalCustomRequestDto customFestivalRequest(String title, int areaCode, LocalDate startDate, LocalDate endDate){
        String overview = "The overview is a section for writing a description of the festival, and it must contain at least 50 characters.";
        return new FestivalCustomRequestDto(
                title, areaCode, "addr1", "addr2",
                new FileUploadRequest(1L,"imageUrl"), null, startDate, endDate, "homePage", overview
        );
    }

    private Festival creatCustomFestival(String title, int areaCode, LocalDate startDate, LocalDate endDate, UserEntity user){
        return new Festival(customFestivalRequest(title, areaCode, startDate, endDate), user);
    }

    private FestivalUpdateRequestDto createUpdateInfo(String title){
        String overview = "The Kakao Tech Campus Festival was held at Pusan National University, and PNU Dev Bros won first place.";
        return new FestivalUpdateRequestDto(    title, 1,
                "update_addr1", "update_addr2", new FileUploadRequest(1L,"update_imageUrl"), null,
                LocalDate.now(), LocalDate.now(), "homePage", overview);
    }

}