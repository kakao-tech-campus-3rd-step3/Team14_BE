package kakao.festapick.festival.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponse;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
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

    @BeforeEach
    void initTestDB(){
        String identifier = "KAKAO_141036";
        UserEntity user = createUser(identifier);
        userRepository.save(user);

        Festival festival1 = createFestival("FESTAPICK_001" , "부산대축제", 1, toLocalDate("20250810"), toLocalDate("20250820"));
        festivalRepository.save(festival1);

        Festival festival2 = createFestival("FESTAPICK_002", "경북대축제", 2, toLocalDate("20250812"), toLocalDate("20250815"));
        festivalRepository.save(festival2);

        Festival festival3 = createFestival("FESTAPICK_003","정컴인축제",  1, toLocalDate("20250814"), toLocalDate("20250817"));
        festivalRepository.save(festival3);

        Festival festival4 = creatCustomFestival("의생공축제",  1, toLocalDate("20250817"), toLocalDate("20250818"), user);
        festivalRepository.save(festival4);

        Festival festival5 = creatCustomFestival("밀양대축제", 3, toLocalDate("20250821"), toLocalDate("20250823"), user);
        festivalRepository.save(festival5);
    }

    @Test
    @WithCustomMockUser(identifier = "KAKAO_141036", role = "ROLE_FESTIVAL_MANAGER")
    @DisplayName("축제 등록 - FESTIVAL_MANAGER")
    void addFestival() throws Exception {

        //given
        CustomFestivalRequestDto requestDto = customFestivalRequest("title", 1, toLocalDate("20250804"), toLocalDate("20250806"));
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
        CustomFestivalRequestDto requestDto = customFestivalRequest("title", 1, toLocalDate("20250804"), toLocalDate("20250806"));
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

        //when-then
        MvcResult mvcResult = mockMvc.perform(get("/api/festivals/area/{areaCode}", areaCode))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        List<FestivalDetailResponse> festivals = Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), FestivalDetailResponse[].class));

        assertAll(
                () -> assertThat(festivals.size()).isEqualTo(2),
                () -> assertThat(festivals.getFirst().areaCode()).isEqualTo(areaCode)
        );
    }

    @Test
    void getFestivalByArea() {
    }

    @Test
    void getApprovedFestivals() {
    }

    @Test
    void getFestivalByKeyword() {
    }

    @Test
    @WithCustomMockUser(identifier = "KAKAO_123456", role = "ROLE_FESTIVAL_MANAGER")
    void updateFestivalInfo() throws Exception {
        //given
        UserEntity user = createUser("KAKAO_123456");
        userRepository.save(user);

        Festival festival = creatCustomFestival("축제1", 1, toLocalDate("20250803"),
                toLocalDate("20250805"), user);
        Festival saved = festivalRepository.save(festival);

        FestivalRequestDto updateInfo = createUpdateInfo("카테캠 축제");
        String updateRequest = objectMapper.writeValueAsString(updateInfo);

        System.out.println("festival = " + festival.getTitle());

        //when-then
        MvcResult mvcResult = mockMvc.perform(patch("/api/festivals/{festivalId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        Festival updatedFestival = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), Festival.class);

        assertAll(
                () -> assertThat(updatedFestival.getId()).isEqualTo(saved.getId()),
                () -> assertThat(updatedFestival.getTitle()).isEqualTo("카테캠 축제")
        );
    }

    @Test
    @WithCustomMockUser(identifier = "KAKAO_123456", role = "ROLE_FESTIVAL_MANAGER")
    void removeFestival() throws Exception {
        //given
        UserEntity user = createUser("KAKAO_123456");
        userRepository.save(user);

        Festival festival = creatCustomFestival("축제1", 1, toLocalDate("20250803"), toLocalDate("20250805"), user);
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
        Festival festival = createFestival("FESTAPICK_999", "카테캠축제", 1, toLocalDate("20250901"), toLocalDate("20250903"));
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

    private Festival createFestival(String contentId, String title, int areaCode, LocalDate startDate, LocalDate endDate){
        return new Festival(FestivalRequest(contentId, title, areaCode, startDate, endDate), "overview", "homePage");
    }

    private CustomFestivalRequestDto customFestivalRequest(String title, int areaCode, LocalDate startDate, LocalDate endDate){
        return new CustomFestivalRequestDto(
                title, areaCode, "addr1", "addr2",
                "imageUrl", startDate, endDate, "homePage", "overView"
        );
    }

    private Festival creatCustomFestival(String title, int areaCode, LocalDate startDate, LocalDate endDate, UserEntity user){
        return new Festival(customFestivalRequest(title, areaCode, startDate, endDate), user);
    }

    private UserEntity createUser(String identifier){
        return new UserEntity(identifier, "example@gmail.com", "exampleName", UserRoleType.FESTIVAL_MANAGER, SocialType.GOOGLE);
    }

    private FestivalRequestDto createUpdateInfo(String title){
        return new FestivalRequestDto(    "contentId", title,
                1, "update_addr1", "update_addr2",
                "update_imageUrl", LocalDate.now(), LocalDate.now());
    }

    private LocalDate toLocalDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

}