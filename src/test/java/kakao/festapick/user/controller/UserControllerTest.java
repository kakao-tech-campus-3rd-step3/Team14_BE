package kakao.festapick.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
import kakao.festapick.chat.repository.QChatMessageRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.fileupload.domain.TemporalFile;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestSecurityContextHolderInjection;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;


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

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private QChatMessageRepository qChatMessageRepository;

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withDrawSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        for (int i=0; i<3; i++) {
            festivalRepository.save(testUtil.createTestFestival(userEntity));
        }

        // when & then
        mockMvc.perform(delete("/api/users")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNoContent());

        Optional<UserEntity> findUser = userRepository.findById(userEntity.getId());
        List<Festival> festivals = festivalRepository.findFestivalByManagerId(userEntity.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(festivals.size()).isEqualTo(0);
            softly.assertThat(findUser.isPresent()).isEqualTo(false);
        });

    }

    @Test
    @DisplayName("회원 탈퇴 시 작성한 모든 메세지 삭제 성공")
    void withDrawSuccess2() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        Festival savedFestival1 =  festivalRepository.save(testUtil.createTourApiTestFestival());
        ChatRoom savedChatRoom1 = chatRoomRepository.save(testUtil.createTestChatRoom(savedFestival1));
        chatParticipantRepository.save(new ChatParticipant(userEntity, savedChatRoom1));

        Festival savedFestival2 =  festivalRepository.save(testUtil.createTourApiTestFestival());
        ChatRoom savedChatRoom2 = chatRoomRepository.save(testUtil.createTestChatRoom(savedFestival2));
        chatParticipantRepository.save(new ChatParticipant(userEntity, savedChatRoom2));

        for (int i=0; i<5; i++) {
            chatMessageRepository.save(new ChatMessage("test message " + i, "image url", savedChatRoom1, userEntity));
            chatMessageRepository.save(new ChatMessage("test message " + i, "image url", savedChatRoom2, userEntity));
        }

        // when & then
        mockMvc.perform(delete("/api/users")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNoContent());

        Optional<UserEntity> findUser = userRepository.findById(userEntity.getId());
        List<ChatMessage> messages1 = qChatMessageRepository.findByChatRoomIdWithUser(savedChatRoom1.getId(), null, null, 1).content();
        List<ChatMessage> messages2 = qChatMessageRepository.findByChatRoomIdWithUser(savedChatRoom2.getId(), null, null, 1).content();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(findUser.isPresent()).isEqualTo(false);
            softly.assertThat(messages1.size()).isEqualTo(0);
            softly.assertThat(messages2.size()).isEqualTo(0);
        });

    }

    @Test
    @DisplayName("본인 정보 조회 성공")
    void getMyInfoSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        ApiResponseDto<UserResponseDto> result = objectMapper.readValue(response, new TypeReference<ApiResponseDto<UserResponseDto>>() {});
        UserResponseDto content = result.content();

        assertSoftly(softly -> {
            softly.assertThat(content.userId()).isEqualTo(userEntity.getId());
            softly.assertThat(content.email()).isEqualTo(userEntity.getEmail());
            softly.assertThat(content.profileImageUrl()).isEqualTo(userEntity.getProfileImageUrl());
            softly.assertThat(content.username()).isEqualTo(userEntity.getUsername());
        });

    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공")
    void changeProfileImageSuccess() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());


        TemporalFile saved = temporalFileRepository.save(new TemporalFile("http://updateImageUrl"));

        FileUploadRequest updateImageUrl = new FileUploadRequest(saved.getId(),"http://updateImageUrl");

        String request = objectMapper.writeValueAsString(updateImageUrl);

        // when
        mockMvc.perform(patch("/api/users/profileImage")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UserEntity findUser = userRepository.findById(userEntity.getId()).get();

        assertThat(findUser.getProfileImageUrl()).isEqualTo(updateImageUrl.presignedUrl());
    }

    @Test
    @DisplayName("축제 관리자 or 어드민인지 조회 - 인증X")
    void checkIsFMOrAdminFalse() throws Exception {

        // when
        String response = mockMvc.perform(get("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<Map<String, Boolean>> apiResponseDto = objectMapper.readValue(response, new TypeReference<ApiResponseDto<Map<String, Boolean>>>() {
        });

        Boolean isFestivalManagerOrAdmin = apiResponseDto.content().get("isFestivalManagerOrAdmin");


        assertThat(isFestivalManagerOrAdmin).isFalse();
    }

    @Test
    @DisplayName("축제 관리자 or 어드민인지 조회 - 축제 관리자")
    void checkIsFMOrAdminTrue() throws Exception {

        // given
        UserEntity userEntity = saveUserEntity();
        userEntity.changeUserRole(UserRoleType.FESTIVAL_MANAGER);
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<Map<String, Boolean>> apiResponseDto = objectMapper.readValue(response, new TypeReference<ApiResponseDto<Map<String, Boolean>>>() {
        });

        Boolean isFestivalManagerOrAdmin = apiResponseDto.content().get("isFestivalManagerOrAdmin");


        assertThat(isFestivalManagerOrAdmin).isTrue();
    }

    private UserEntity saveUserEntity() {

        return userRepository.save(testUtil.createTestUser(identifier));
    }

}
