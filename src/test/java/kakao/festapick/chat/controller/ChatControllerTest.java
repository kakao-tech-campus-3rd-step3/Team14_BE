package kakao.festapick.chat.controller;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.PreviousMessagesResponseDto;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestSecurityContextHolderInjection;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChatControllerTest {

    private static final String identifier = "GOOGLE_1234";
    private final TestUtil testUtil = new TestUtil();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FestivalRepository festivalRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatRoomSuccess() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();

        mockMvc.perform(post(String.format("/api/festivals/%s/chatRooms", festival.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk());

        Optional<ChatRoom> find = chatRoomRepository.findByFestivalId(festival.getId());

        assertThat(find).isPresent();
        ChatRoom actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getRoomName())
                        .isEqualTo(festival.getTitle() + " 채팅방")
        );
    }

    @Test
    @DisplayName("채팅방 등록 실패 (없는 축제에 대한 채팅방 생성 시도)")
    void createChatRoomFail() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        mockMvc.perform(post(String.format("/api/festivals/%s/chatRooms", 999L))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("채팅방의 이전 대화내역 불러오기 성공")
    void getPreviousMessagesSuccess() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        ChatMessage chatMessage = new ChatMessage("test message", "image url", savedChatRoom,
                userEntity);
        chatMessageRepository.save(chatMessage);

        mockMvc.perform(get(String.format("/api/chatRooms/%s/messages", savedChatRoom.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("cursorId로 채팅방의 이전 대화내역 불러오기 성공")
    void getPreviousMessagesWithCursorIdSuccess() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        ChatMessage firstChatMessage = new ChatMessage("test message1", "image url", savedChatRoom,
                userEntity);
        ChatMessage secondChatMessage = new ChatMessage("test message2", "image url", savedChatRoom,
                userEntity);
        ChatMessage thirdChatMessage = new ChatMessage("test message3", "image url", savedChatRoom,
                userEntity);

        chatMessageRepository.save(firstChatMessage);
        chatMessageRepository.save(secondChatMessage);
        chatMessageRepository.save(thirdChatMessage);

        entityManager.flush();

        entityManager.refresh(firstChatMessage);
        entityManager.refresh(secondChatMessage);
        entityManager.refresh(thirdChatMessage);

        String response = mockMvc.perform(get(String.format("/api/chatRooms/%s/messages", savedChatRoom.getId()))
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .param("cursor", String.valueOf(thirdChatMessage.getId()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        PreviousMessagesResponseDto apiResponse = objectMapper.readValue(response, new TypeReference<PreviousMessagesResponseDto>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.content().size()).isEqualTo(2);
            softly.assertThat(apiResponse.content().get(0).content()).isEqualTo(firstChatMessage.getContent());
            softly.assertThat(apiResponse.content().get(1).content()).isEqualTo(secondChatMessage.getContent());
        });

    }

    @Test
    @DisplayName("내 채팅방 조회 시 읽지않은 메시지가 있는 채팅방은 표시 성공")
    void getMyChatRoomReadStatus() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        Festival festival = saveFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        ChatParticipant chatParticipant = chatParticipantRepository.save(
                new ChatParticipant(userEntity, savedChatRoom)
        );

        savedChatRoom.updateMessageSeq();
        savedChatRoom.updateMessageSeq();

        String response = mockMvc.perform(get("/api/chatRooms/me")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Boolean exist = read(response, "$.content[0].existNewMessage");

        assertSoftly(softly -> {
            softly.assertThat(exist.equals(true));
        });

    }

    private UserEntity saveUserEntity() {
        return userRepository.save(testUtil.createTestUser(identifier));
    }

    private Festival saveFestival() throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg",
                testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, testUtil.createTourDetailResponse());

        return festivalRepository.save(festival);
    }
}
