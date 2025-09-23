package kakao.festapick.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import kakao.festapick.dto.ApiResponseDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.domain.TemporalFile;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.SoftAssertions;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemporalFileRepository temporalFileRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String identifier = "GOOGLE_1234";
    @Autowired
    private TestUtil testUtil;

    @Test
    @DisplayName("회원 탈퇴 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void withDrawSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();

        for (int i=0; i<3; i++) {
            festivalRepository.save(testUtil.createTestFestival(userEntity));
        }

        // when & then
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isNoContent());

        Optional<UserEntity> findUser = userRepository.findById(userEntity.getId());
        List<Festival> festivals = festivalRepository.findFestivalByManagerId(userEntity.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(festivals.size()).isEqualTo(0);
            softly.assertThat(findUser.isPresent()).isEqualTo(false);
        });

    }

    @Test
    @DisplayName("본인 정보 조회 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void getMyInfoSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();

        // when
        String response = mockMvc.perform(get("/api/users/my"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        ApiResponseDto<UserResponseDto> result = objectMapper.readValue(response, new TypeReference<ApiResponseDto<UserResponseDto>>() {});
        UserResponseDto content = result.content();

        assertSoftly(softly -> {
            softly.assertThat(content.email()).isEqualTo(userEntity.getEmail());
            softly.assertThat(content.profileImageUrl()).isEqualTo(userEntity.getProfileImageUrl());
            softly.assertThat(content.username()).isEqualTo(userEntity.getUsername());
        });

    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void changeProfileImageSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();


        TemporalFile saved = temporalFileRepository.save(new TemporalFile("http://updateImageUrl"));

        FileUploadRequest updateImageUrl = new FileUploadRequest(saved.getId(),"http://updateImageUrl");

        String request = objectMapper.writeValueAsString(updateImageUrl);

        // when
        mockMvc.perform(patch("/api/users/profileImage")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UserEntity findUser = userRepository.findById(userEntity.getId()).get();

        assertThat(findUser.getProfileImageUrl()).isEqualTo(updateImageUrl.presignedUrl());
    }

    private UserEntity saveUserEntity() {

        return userRepository.save(testUtil.createTestUser(identifier));
    }

}
