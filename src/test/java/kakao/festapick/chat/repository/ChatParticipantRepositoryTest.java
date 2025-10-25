package kakao.festapick.chat.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import kakao.festapick.chat.domain.ChatParticipant;
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
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
public class ChatParticipantRepositoryTest {

    private static final String identifier = "GOOGLE-1234";
    private final TestUtil testUtil = new TestUtil();
    private final FestivalRequestDto requestDto = new FestivalRequestDto("1234567", "test festival",
            11, "test addr1", "test addr2", "http://asd.test.com/example.jpg",
            testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
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
    @DisplayName("채팅 참여자 등록 성공 테스트")
    void createChatParticipantSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatParticipant actual = chatParticipantRepository.save(
                new ChatParticipant(userEntity, chatRoom));

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getChatRoom())
                        .isEqualTo(chatRoom),
                () -> AssertionsForClassTypes.assertThat(actual.getMessageSeq())
                        .isEqualTo(chatRoom.getMessageSeq())
        );
    }

    @Test
    @DisplayName("채팅 참여자 확인 테스트")
    void checkChatParticipant() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        chatParticipantRepository.save(new ChatParticipant(userEntity, chatRoom));

        boolean actual = chatParticipantRepository.existsByUserAndChatRoom(userEntity, chatRoom);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual).isTrue()
        );
    }

    @Test
    @DisplayName("유저 아이디와 채팅방 아이디로 채팅 참여자 찾기 테스트")
    void findChatParticipant() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatParticipant chatParticipant = chatParticipantRepository.save(new ChatParticipant(userEntity, chatRoom));

        Optional<ChatParticipant> find = chatParticipantRepository.findByChatRoomIdAndUserIdWithChatRoom(chatRoom.getId(), userEntity.getId());

        AssertionsForClassTypes.assertThat(find).isPresent();

        ChatParticipant actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual).isEqualTo(chatParticipant)
        );
    }
}
