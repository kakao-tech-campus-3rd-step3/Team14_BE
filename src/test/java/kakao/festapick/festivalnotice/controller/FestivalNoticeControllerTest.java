package kakao.festapick.festivalnotice.controller;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festivalnotice.Repository.FestivalNoticeRepository;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.festivalnotice.dto.FestivalNoticeRequestDto;
import kakao.festapick.festivalnotice.dto.FestivalNoticeResponseDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class FestivalNoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FestivalNoticeRepository festivalNoticeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("축제에 대한 새로운 공지를 작성")
    void addNotice() throws Exception {
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1021118"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Festival festival = festivalRepository.save(testUtil.createTestFestival(user));

        FestivalNoticeRequestDto requestDto = new FestivalNoticeRequestDto("공지사항 1", "전달 내용", new ArrayList<>());
        String requestJson = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post(URI.create("/api/festivals/" + festival.getId() + "/notices"))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }

    @Test
    @DisplayName("축제에 대한 새로운 공지를 작성 - 실패(다른 사람이 작성)")
    void addNoticeFail() throws Exception {
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1021118"));
        Festival festival = festivalRepository.save(testUtil.createTestFestival(user));

        UserEntity user2 = userRepository.save(testUtil.createTestManager("KAKAO-1021150"));
        TestSecurityContextHolderInjection.inject(user2.getId(), user2.getRoleType());

        FestivalNoticeRequestDto requestDto = new FestivalNoticeRequestDto("공지사항 1", "전달 내용", new ArrayList<>());
        String requestJson = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post(URI.create("/api/festivals/" + festival.getId() + "/notices"))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("축제에 대한 모든 공지 사항 가져오기")
    void getFestivalNotices() throws Exception {
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1021118"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Festival festival = festivalRepository.save(testUtil.createTestFestival(user));

        createFestivalNotice("공지사항 1", "내용 1", festival, user);
        createFestivalNotice("공지사항 2", "내용 2", festival, user);
        createFestivalNotice("공지사항 3", "내용 3", festival, user);

        MvcResult mvcResult = mockMvc.perform(get(URI.create("/api/festivals/" + festival.getId() + "/notices"))
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        //then
        String response = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> festivalPermissions = read(response, "$.content");

        assertAll(
                () -> assertThat(festivalPermissions.size()).isEqualTo(3),
                () -> assertThat(festivalPermissions.stream().allMatch(p -> p.get("title").contains("공지사항"))).isEqualTo(
                        true)
        );

    }

    @Test
    @DisplayName("공지 사항을 수정")
    void updateFestivalNotice() throws Exception {
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1021118"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Festival festival = festivalRepository.save(testUtil.createTestFestival(user));
        FestivalNotice festivalNotice = createFestivalNotice("공지사항 1", "내용 1", festival, user);

        FestivalNoticeRequestDto requestDto = new FestivalNoticeRequestDto("수정된 공지사항", "전달 내용", new ArrayList<>());
        String requestJson = objectMapper.writeValueAsString(requestDto);

        MvcResult mvcResult = mockMvc.perform(put(URI.create("/api/festivals/notices/" + festivalNotice.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        ApiResponseDto<FestivalNoticeResponseDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        FestivalNoticeResponseDto content = response.content();

        assertThat(content.title()).isEqualTo("수정된 공지사항");
    }

    @Test
    @DisplayName("작성한 공지사항을 삭제")
    void removeFestivalNotice() throws Exception {
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1021118"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Festival festival = festivalRepository.save(testUtil.createTestFestival(user));
        FestivalNotice festivalNotice = createFestivalNotice("공지사항 1", "내용 1", festival, user);

        mockMvc.perform(delete(URI.create("/api/festivals/notices/" + festivalNotice.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                ).andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    private FestivalNotice createFestivalNotice(String title, String content, Festival festival, UserEntity user) {
        FestivalNoticeRequestDto festivalNoticeRequestDto = new FestivalNoticeRequestDto(title, content,
                new ArrayList<>());
        return festivalNoticeRepository.save(new FestivalNotice(festivalNoticeRequestDto, festival, user));
    }


}