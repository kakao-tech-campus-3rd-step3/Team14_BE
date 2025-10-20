package kakao.festapick.permission.festivalpermission.controller;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionDetailDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionRequestDto;
import kakao.festapick.permission.festivalpermission.repository.FestivalPermissionRepository;
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
class FestivalPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FestivalPermissionRepository festivalPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("기존 축제에 관리자 권한 신청")
    void applyFestivalPermission() throws Exception {

        //given
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1234"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        Festival festival = festivalRepository.save(testUtil.createTourApiTestFestival());

        FestivalPermissionRequestDto requestDto = createRequestDto();
        String requestString = objectMapper.writeValueAsString(requestDto);

        //when - then
        mockMvc.perform(post("/api/festival-permissions/festival/" + festival.getId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }

    @Test
    @DisplayName("나의 신청 목록 모두 가져오기")
    void getMyFestivalPermissions() throws Exception {

        //given
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1234"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        createFestivalPermission(user);
        Festival festival2 = testUtil.createTourApiTestFestival2();
        festivalRepository.save(festival2);
        festivalPermissionRepository.save(new FestivalPermission(user, festival2));

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/festival-permissions/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        //then
        String response = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> festivalPermissions = read(response, "$.content");

        assertAll(
                () -> assertThat(festivalPermissions.size()).isEqualTo(2),
                () -> assertThat(festivalPermissions.stream().anyMatch(p -> p.get("title").equals("카테캠축제"))).isEqualTo(true)
        );
    }

    @Test
    @DisplayName("단건 조회")
    void getFestivalPermission() throws Exception {

        //given
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1234"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Long id = createFestivalPermission(user);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/festival-permissions/" + id)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        //then
        ApiResponseDto<FestivalPermissionDetailDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        FestivalPermissionDetailDto content = response.content();

        assertAll(
                () -> assertThat(content.state()).isEqualTo(PermissionState.PENDING),
                () -> assertThat(content.title()).isEqualTo("부산대축제")
        );
    }

    @Test
    @DisplayName("신청서 업데이트 - (증빙 서류 업데이트)")
    void updateFestivalPermission() throws Exception {
        //given
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1234"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Long id = createFestivalPermission(user);

        FestivalPermissionRequestDto requestDto = createRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/api/festival-permissions/" + id)
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        //then
        ApiResponseDto<FestivalPermissionDetailDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        FestivalPermissionDetailDto content = response.content();

        assertAll(
                () -> assertThat(content.state()).isEqualTo(PermissionState.PENDING),
                () -> assertThat(content.docs().size()).isEqualTo(requestDto.documents().size()),
                () -> assertThat(content.docs().stream().anyMatch(s -> s.equals("https://documents1.com"))).isEqualTo(true)
        );
    }

    @Test
    @DisplayName("신청서 삭제")
    void removeFestivalPermission() throws Exception {
        //given
        UserEntity user = userRepository.save(testUtil.createTestManager("KAKAO-1234"));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        Long id = createFestivalPermission(user);

        //when - then
        mockMvc.perform(delete("/api/festival-permissions/" + id)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    private Long createFestivalPermission(UserEntity user){
        Festival festival1 = testUtil.createTourApiTestFestival();
        festivalRepository.save(festival1);
        return festivalPermissionRepository.save(new FestivalPermission(user, festival1)).getId();
    }

    private FestivalPermissionRequestDto createRequestDto(){
        List<FileUploadRequest> uploadRequests = new ArrayList<>();
        uploadRequests.add(new FileUploadRequest(123L, "https://documents1.com"));
        uploadRequests.add(new FileUploadRequest(124L, "https://documents2.com"));
        return new FestivalPermissionRequestDto(uploadRequests);
    }
}