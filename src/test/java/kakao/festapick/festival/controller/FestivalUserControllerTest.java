package kakao.festapick.festival.controller;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestSecurityContextHolderInjection;
import kakao.festapick.util.TestUtil;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.repository.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FestivalUserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private FestivalRepository festivalRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private WishRepository wishRepository;

    @Autowired private ChatRoomRepository chatRoomRepository;

    @Autowired private ChatParticipantRepository chatParticipantRepository;

    @Autowired private ChatMessageRepository chatMessageRepository;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private TestUtil testUtil;

    private  UserEntity testManager;


    @BeforeEach
    void initTestDB() throws Exception {
        String identifier = "KAKAO_141036";
        testManager = testUtil.createTestManager(identifier);
        userRepository.save(testManager);

        Festival festival1 = createFestival("FESTAPICK_001" , "부산대축제", 1, testUtil.toLocalDate("20250810"), testUtil.toLocalDate("20250820"));
        festivalRepository.save(festival1);

        Festival festival2 = createFestival("FESTAPICK_002", "경북대축제", 2, testUtil.toLocalDate("20250812"), testUtil.toLocalDate("20250815"));
        festivalRepository.save(festival2);

        Festival festival3 = createFestival("FESTAPICK_003","정컴인축제",  1, testUtil.toLocalDate("20250814"), testUtil.toLocalDate("20250817"));
        festivalRepository.save(festival3);

        Festival festival4 = createCustomFestival("의생공축제",  1, testUtil.toLocalDate("20250817"), testUtil.toLocalDate("20250818"), testManager);
        festivalRepository.save(festival4);

        Festival festival5 = createCustomFestival("밀양대축제", 3, testUtil.toLocalDate("20250821"), testUtil.toLocalDate("20250823"), testManager);
        festivalRepository.save(festival5);
    }

    @Test
    @DisplayName("축제 등록 - FESTIVAL_MANAGER")
    void addFestival() throws Exception {

        TestSecurityContextHolderInjection.inject(testManager.getId(), testManager.getRoleType());

        //given
        FestivalCustomRequestDto requestDto = customFestivalRequest("title", 1, testUtil.toLocalDate("20250804"), testUtil.toLocalDate("20250806"));
        String customRequestDto = objectMapper.writeValueAsString(requestDto);

        //when-then
        mockMvc.perform(post("/api/festivals")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(customRequestDto)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }


    @Test
    @DisplayName("축제 등록 실패 - USER")
    void addFestivalFail() throws Exception {

        UserEntity user = testUtil.createTestUser();
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        //given
        FestivalCustomRequestDto requestDto = customFestivalRequest("title", 1, testUtil.toLocalDate("20250804"), testUtil.toLocalDate("20250806"));
        String customRequestDto = objectMapper.writeValueAsString(requestDto);

        //when-then
        mockMvc.perform(post("/api/festivals")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(customRequestDto)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }


    @Test
    @DisplayName("현재 특정 지역에서 참여할 수 있는 모든 축제를 조회")
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
                () -> assertThat(content.addr2()).isNotNull(),
                () -> assertThat(content.imageInfos()).isInstanceOf(List.class)
        );
    }

    @Test
    @DisplayName("내가 등록한 축제를 조회")
    void getMyFestivals() throws Exception {

        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);

        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        Festival festival1 = createCustomFestival("정컴축제", 12, testUtil.toLocalDate("20250605"), testUtil.toLocalDate("20250624"), user);
        festivalRepository.save(festival1);

        Festival festival2 = createCustomFestival("카테캠축제", 12, testUtil.toLocalDate("20250605"), testUtil.toLocalDate("20250624"), user);
        festivalRepository.save(festival2);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/festivals/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> festivals = read(response, "$.content");

        assertAll(
                () -> assertThat(festivals.size()).isEqualTo(2),
                () -> assertThat(festivals.stream().anyMatch(info -> info.get("title").equals("카테캠축제")))
        );
    }

    @Test
    @DisplayName("축제의 정보를 수정 - 포스터를 변경")
    void updateFestivalInfo() throws Exception {
        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        Festival festival = createCustomFestival("축제1", 1, testUtil.toLocalDate("20250803"), testUtil.toLocalDate("20250805"), user);
        Festival saved = festivalRepository.save(festival);

        FestivalUpdateRequestDto updateInfo = createUpdateInfo("카테캠 축제 시즌 3", "카테캠 포스터", null);
        String updateRequest = objectMapper.writeValueAsString(updateInfo);

        //when-then
        MvcResult mvcResult = mockMvc.perform(patch("/api/festivals/{festivalId}", saved.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        ApiResponseDto<FestivalDetailResponseDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<ApiResponseDto<FestivalDetailResponseDto>>() {});
        FestivalDetailResponseDto content = response.content();

        assertAll(
                () -> assertThat(content.id()).isEqualTo(saved.getId()),
                () -> assertThat(content.title()).isEqualTo("카테캠 축제 시즌 3"),
                () -> assertThat(content.posterInfo()).isEqualTo("카테캠 포스터")
        );
    }

    @Test
    @DisplayName("내가 등록한 축제의 이미지 정보를 변경")
    void updatePoster() throws Exception {

        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        Festival festival = createCustomFestival("정컴축제", 15, testUtil.toLocalDate("20250801"), testUtil.toLocalDate("20250810"), user);
        Festival saved = festivalRepository.save(festival);

        List<FileUploadRequest> images = new ArrayList<>();
        images.add(new FileUploadRequest(99L, "https://festapick.firstimage.com"));
        images.add(new FileUploadRequest(9999L,"https://festapick.newimage.com"));

        FestivalUpdateRequestDto festivalUpdateRequestDto = createUpdateInfo("카테캠 축제", "카테캠 포스터", images);
        String updateRequest = objectMapper.writeValueAsString(festivalUpdateRequestDto);

        //when-then
        MvcResult mvcResult = mockMvc.perform(patch("/api/festivals/{festivalId}", saved.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        ApiResponseDto<FestivalDetailResponseDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        FestivalDetailResponseDto content = response.content();

        assertAll(
                () -> assertThat(content.imageInfos().size()).isEqualTo(2),
                () -> assertThat(content.imageInfos().contains("https://festapick.newimage.com")),
                () -> assertThat(content.imageInfos().contains("https://festapick.firstimage.com"))
        );
    }



    @Test
    @DisplayName("축제 삭제 성공")
    void removeFestivalSuccess() throws Exception {
        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        Festival festival = createCustomFestival("축제1", 1, testUtil.toLocalDate("20250803"), testUtil.toLocalDate("20250805"), user);
        Festival saved = festivalRepository.save(festival);

        for (int i=0; i<10; i++) {
            UserEntity testUser = testUtil.createTestUser("KAKAO_" + i);
            userRepository.save(testUser);
            wishRepository.save(new Wish(testUser, saved));
        }

        //when-then
        mockMvc.perform(delete("/api/festivals/{festivalId}", saved.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        boolean isEmpty = festivalRepository.findFestivalById(saved.getId()).isEmpty();
        List<Wish> wishes = wishRepository.findByFestivalId(saved.getId());


        assertSoftly(softly -> {
            softly.assertThat(isEmpty).isTrue();
            softly.assertThat(wishes).isEmpty();
        });
    }

    @Test
    @DisplayName("축제 삭제 성공 (채팅방 있는 경우)")
    void removeFestivalSuccess2() throws Exception {
        //given
        UserEntity user = testUtil.createTestManager("KAKAO_123456");
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        Festival festival = createCustomFestival("축제1", 1, testUtil.toLocalDate("20250803"), testUtil.toLocalDate("20250805"), user);
        Festival savedFestival = festivalRepository.save(festival);

        ChatRoom chatRoom = testUtil.createTestChatRoom(savedFestival);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        Long chatRooomId = savedChatRoom.getId();

        for (int i=0; i<10; i++) {
            UserEntity testUser = testUtil.createTestUser("KAKAO_" + i);
            userRepository.save(testUser);
            wishRepository.save(new Wish(testUser, savedFestival));
            chatParticipantRepository.save(new ChatParticipant(testUser, savedChatRoom));
            chatMessageRepository.save(new ChatMessage("test message", "image url",savedChatRoom, testUser));
        }

        //when-then
        mockMvc.perform(delete("/api/festivals/{festivalId}", savedFestival.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        boolean festivalIsEmpty = festivalRepository.findFestivalById(savedFestival.getId()).isEmpty();
        boolean chatRoomIsEmpty = chatRoomRepository.findByFestivalId(savedFestival.getId()).isEmpty();
        List<Wish> wishes = wishRepository.findByFestivalId(savedFestival.getId());
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomId(chatRooomId);
        List<ChatMessage> messages = chatMessageRepository.findAllByChatRoomId(chatRooomId);

        assertSoftly(softly -> {
            softly.assertThat(festivalIsEmpty).isTrue();
            softly.assertThat(chatRoomIsEmpty).isTrue();
            softly.assertThat(wishes).isEmpty();
            softly.assertThat(chatParticipants).isEmpty();
            softly.assertThat(messages).isEmpty();
        });
    }

    @Test
    @DisplayName("User가 축제를 삭제하려고 하는 경우 - 삭제 실패")
    void removeFestivalFail() throws Exception {
        //given
        UserEntity user = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Festival festival = createFestival("FESTAPICK_999", "카테캠축제", 1, testUtil.toLocalDate("20250901"), testUtil.toLocalDate("20250903"));
        Festival saved = festivalRepository.save(festival);

        //when-then
        mockMvc.perform(delete("/api/festivals/{festivalId}", saved.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("축제 title로 축제를 검색")
    void searchFestival() throws Exception {

        //given
        String keyword = "정컴인";

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/festivals").param("keyword", keyword))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> festivals = read(response, "$.content");

        //then
        assertAll(
                () -> assertThat(festivals.size()).isEqualTo(1),
                () -> assertThat(festivals.getFirst().get("title")).isEqualTo("정컴인축제")
        );
    }

    @Test
    @DisplayName("축제 title로 축제를 검색 - 공백은 허용하지 않음")
    void searchFestivalFail() throws Exception {

        //given
        String keyword = "  ";

        //when - then
        mockMvc.perform(get("/api/festivals").param("keyword", keyword))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
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
                new FileUploadRequest(1L,"imageUrl"), testUtil.createFestivalImages(), startDate, endDate, "homePage", overview
        );
    }

    private Festival createCustomFestival(String title, int areaCode, LocalDate startDate, LocalDate endDate, UserEntity user){
        return new Festival(customFestivalRequest(title, areaCode, startDate, endDate), user);
    }

    private FestivalUpdateRequestDto createUpdateInfo(String title, String posterInfo, List<FileUploadRequest> imageInfos){
        String overview = "The Kakao Tech Campus Festival was held at Pusan National University, and PNU Dev Bros won first place.";
        return new FestivalUpdateRequestDto(title, 1,
                "update_addr1", "update_addr2", new FileUploadRequest(1L,posterInfo), imageInfos,
                LocalDate.now(), LocalDate.now(), "homePage", overview);
    }



}
