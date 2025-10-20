package kakao.festapick.permission.fmpermission.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.permission.fmpermission.domain.FMPermission;
import kakao.festapick.permission.fmpermission.dto.FMPermissionRequestDto;
import kakao.festapick.permission.fmpermission.dto.FMPermissionResponseDto;
import kakao.festapick.permission.fmpermission.repository.FMPermissionRepository;
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
class FMPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FMPermissionRepository fmPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("FM로 role 변경을 신청")
    void applyFMPermission() throws Exception {

        //given
        UserEntity user = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        FMPermissionRequestDto requestDto = createFMPermissionRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //when-then
        mockMvc.perform(post("/api/fm-permissions")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }

    @Test
    @DisplayName("나의 FMPermission 조회하기")
    void getMyFMPermission() throws Exception {
        //given
        UserEntity user = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        FMPermission fmPermission = fmPermissionRepository.save(new FMPermission(user, "부산대학교"));
        createAndSaveDocs(fmPermission.getId());

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/fm-permissions/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        //then
        ApiResponseDto<FMPermissionResponseDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        FMPermissionResponseDto content = response.content();
        assertAll(
                () -> assertThat(content.department()).isEqualTo("부산대학교"),
                () -> assertThat(content.docsUrls().size()).isEqualTo(2),
                () -> assertThat(content.id()).isNotNull()
        );
    }

    @Test
    @DisplayName("작성한 FMPermission에 대해 document 수정하기")
    void updateDocument() throws Exception {
        //given
        UserEntity user = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        FMPermission fmPermission = fmPermissionRepository.save(new FMPermission(user, "부산대학교"));
        createAndSaveDocs(fmPermission.getId());

        FMPermissionRequestDto updateDto = createFMPermissionRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        //when
        MvcResult mvcResult = mockMvc.perform(put("/api/fm-permissions/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        //then
        ApiResponseDto<FMPermissionResponseDto> response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        FMPermissionResponseDto content = response.content();
        assertAll(
                () -> assertThat(content.docsUrls().size()).isEqualTo(3),
                () -> assertThat(content.docsUrls().getFirst()).contains("https://documents")
        );

    }

    @Test
    @DisplayName("내가 작성한 FMPermission 삭제하기")
    void cancelFMPermission() throws Exception {
        //given
        UserEntity user = userRepository.save(testUtil.createTestUser());
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        FMPermission fmPermission = fmPermissionRepository.save(new FMPermission(user, "부산대학교"));
        createAndSaveDocs(fmPermission.getId());

        mockMvc.perform(delete("/api/fm-permissions/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                .andReturn();

        Optional<FMPermission> result = fmPermissionRepository.findFMPermissionById(fmPermission.getId());
        assertThat(result).isEmpty();
    }

    private List<FileEntity> createAndSaveDocs(Long id){
        List<FileEntity> docs = new ArrayList<>();
        docs.add(new FileEntity("https://docs1.com", FileType.DOCUMENT, DomainType.FM_PERMISSION, id));
        docs.add(new FileEntity("https://docs2.com", FileType.DOCUMENT, DomainType.FM_PERMISSION, id));
        fileService.saveAll(docs);
        return docs;
    }

    private FMPermissionRequestDto createFMPermissionRequestDto() {
        List<FileUploadRequest> fileUploadRequests = new ArrayList<>();
        fileUploadRequests.add(new FileUploadRequest(99L, "https://documents1.com"));
        fileUploadRequests.add(new FileUploadRequest(999L, "https://documents2.com"));
        fileUploadRequests.add(new FileUploadRequest(9999L, "https://documents3.com"));
        return new FMPermissionRequestDto("부산대학교", fileUploadRequests);
    }

}
