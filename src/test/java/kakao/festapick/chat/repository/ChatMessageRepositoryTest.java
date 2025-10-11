package kakao.festapick.chat.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
public class ChatMessageRepositoryTest {

    private static final String identifier = "GOOGLE-1234";
    private final TestUtil testUtil = new TestUtil();
    private final FestivalRequestDto requestDto = new FestivalRequestDto("1234567", "test festival",
            11, "test addr1", "test addr2", "http://asd.test.com/example.jpg",
            testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FestivalRepository festivalRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private UserEntity saveUserEntity() {
        return userRepository.save(testUtil.createTestUser(identifier));
    }

    private Festival saveFestival() throws Exception {
        return festivalRepository.save(
                new Festival(requestDto, testUtil.createTourDetailResponse()));
    }

    private ChatRoom saveChatRoom(Festival festival) throws Exception {
        return chatRoomRepository.save(testUtil.createTestChatRoom(festival));
    }

    @Test
    @DisplayName("메세지 등록 성공 테스트")
    void createMessageSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage actual = chatMessageRepository.save(
                new ChatMessage("test message", "image url",chatRoom, userEntity));

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getContent())
                        .isEqualTo("test message")
        );
    }

    @Test
    @DisplayName("채팅방 메세지 조회 성공 테스트")
    void getPreviousMessageSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        chatMessageRepository.save(new ChatMessage("test message", "image url", chatRoom, userEntity));

        Page<ChatMessage> find = chatMessageRepository.findByChatRoomId(chatRoom.getId(), PageRequest.of(0, 1));
        ChatMessage actual = find.getContent().get(0);
        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getContent())
                        .isEqualTo("test message")
        );
    }
}
