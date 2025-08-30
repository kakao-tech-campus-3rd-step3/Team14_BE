package kakao.festapick.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.ProfileImageUpdateRequest;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.repository.UserRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String identifier = "GOOGLE_1234";

    @Test
    @DisplayName("회원 탈퇴 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void withDrawSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();

        // when & then
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isNoContent());

        Optional<UserEntity> findUser = userRepository.findById(userEntity.getId());

        assertThat(findUser.isPresent()).isEqualTo(false);
    }

    @Test
    @DisplayName("본인 정보 조회 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void getMyInfoSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();

        // when
        String response = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        UserResponseDto userResponseDto = objectMapper.readValue(response, UserResponseDto.class);

        assertSoftly(softly -> {
            softly.assertThat(userResponseDto.email()).isEqualTo(userEntity.getEmail());
            softly.assertThat(userResponseDto.profileImageUrl()).isEqualTo(userEntity.getProfileImageUrl());
            softly.assertThat(userResponseDto.username()).isEqualTo(userEntity.getUsername());
        });

    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void changeProfileImageSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();

        ProfileImageUpdateRequest updateImageUrl = new ProfileImageUpdateRequest("updateImageUrl");

        String request = objectMapper.writeValueAsString(updateImageUrl);

        // when
        mockMvc.perform(patch("/api/users/profileImage")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UserEntity findUser = userRepository.findById(userEntity.getId()).get();

        assertThat(findUser.getProfileImageUrl()).isEqualTo(updateImageUrl.imageUrl());
    }

    private UserEntity saveUserEntity() {

        return userRepository.save(new UserEntity(identifier,
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE));
    }



}