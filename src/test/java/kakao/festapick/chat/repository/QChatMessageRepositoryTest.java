package kakao.festapick.chat.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatMessageSliceDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
@EnableJpaAuditing
public class QChatMessageRepositoryTest {

    private static final String identifier = "GOOGLE-1234";
    private final TestUtil testUtil = new TestUtil();
    private final FestivalRequestDto requestDto = new FestivalRequestDto("1234567", "test festival",
            11, "test addr1", "test addr2", "http://asd.test.com/example.jpg",
            testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));

    private QChatMessageRepository qChatMessageRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FestivalRepository festivalRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        qChatMessageRepository = new QChatMessageRepository(entityManager);
    }

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

    private void updateCreatedDate(EntityManager entityManager, Long chatMessageId,
            LocalDateTime time) {
        entityManager.createQuery(
                        "update ChatMessage cm set cm.createdDate = :time where cm.id = :chatMessageId")
                .setParameter("time", time)
                .setParameter("chatMessageId", chatMessageId)
                .executeUpdate();
    }

    @Test
    @DisplayName("채팅방 메세지 초기 조회 성공 테스트")
    void getPreviousMessageSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage savedMessage = chatMessageRepository.save(
                new ChatMessage("test message", "image url", chatRoom, userEntity)
        );

        ChatMessageSliceDto find = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), null, null, 1
        );

        ChatMessage actual = find.content().get(0);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId())
                        .isEqualTo(savedMessage.getId()),
                () -> AssertionsForClassTypes.assertThat(actual.getUser())
                        .isEqualTo(savedMessage.getUser()),
                () -> AssertionsForClassTypes.assertThat(actual.getContent())
                        .isEqualTo(savedMessage.getContent())
        );
    }

    @Test
    @DisplayName("이전 채팅방 메세지 조회 성공 테스트")
    void getPreviousMessageSuccess2() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage firstSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 1", "image url 1", chatRoom, userEntity)
        );

        ChatMessage secondSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 2", "image url 2", chatRoom, userEntity)
        );

        entityManager.flush();
        entityManager.refresh(firstSavedMessage);
        entityManager.refresh(secondSavedMessage);

        ChatMessageSliceDto find = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), secondSavedMessage.getId(), secondSavedMessage.getCreatedDate(), 1
        );

        ChatMessage actual = find.content().get(0);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId())
                        .isEqualTo(firstSavedMessage.getId()),
                () -> AssertionsForClassTypes.assertThat(actual.getUser().getId())
                        .isEqualTo(firstSavedMessage.getUser().getId()),
                () -> AssertionsForClassTypes.assertThat(actual.getContent())
                        .isEqualTo(firstSavedMessage.getContent())
        );
    }

    @Test
    @DisplayName("cursor 이전 메시지가 size 만큼 존재하면 hasNext는 false 테스트")
    void getPreviousMessageSuccess3() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage firstSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 1", "image url 1", chatRoom, userEntity)
        );

        ChatMessage secondSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 2", "image url 2", chatRoom, userEntity)
        );

        ChatMessage thirdSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 3", "image url 3", chatRoom, userEntity)
        );

        entityManager.flush();
        entityManager.refresh(firstSavedMessage);
        entityManager.refresh(secondSavedMessage);
        entityManager.refresh(thirdSavedMessage);

        ChatMessageSliceDto actual1 = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), thirdSavedMessage.getId(), thirdSavedMessage.getCreatedDate(), 2
        );

        ChatMessageSliceDto actual2 = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), thirdSavedMessage.getId(), thirdSavedMessage.getCreatedDate(), 1
        );

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual1.hasNext()).isEqualTo(false),
                () -> AssertionsForClassTypes.assertThat(actual2.hasNext()).isEqualTo(true)
        );

    }

    @Test
    @DisplayName("이전 메시지 없으면 빈 결과 반환 테스트")
    void getPreviousMessageSuccess4() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage firstSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 1", "image url 1", chatRoom, userEntity)
        );

        ChatMessage secondSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 2", "image url 2", chatRoom, userEntity)
        );

        ChatMessage thirdSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 3", "image url 3", chatRoom, userEntity)
        );

        entityManager.flush();
        entityManager.refresh(firstSavedMessage);
        entityManager.refresh(secondSavedMessage);
        entityManager.refresh(thirdSavedMessage);

        ChatMessageSliceDto actual = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), firstSavedMessage.getId(), firstSavedMessage.getCreatedDate(), 1
        );

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.hasNext()).isEqualTo(false),
                () -> AssertionsForClassTypes.assertThat(actual.content().isEmpty()).isEqualTo(true)
        );

    }

    @Test
    @DisplayName("페이징 결과물은 createdDate 내림차순으로 정렬된다")
    void getPreviousMessageSuccess5() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage firstSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 1", "image url 1", chatRoom, userEntity)
        );

        ChatMessage secondSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 2", "image url 2", chatRoom, userEntity)
        );

        ChatMessage thirdSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 3", "image url 3", chatRoom, userEntity)
        );

        LocalDateTime base = LocalDateTime.now();
        updateCreatedDate(entityManager, firstSavedMessage.getId(), base.minusSeconds(1));
        updateCreatedDate(entityManager, secondSavedMessage.getId(), base.minusSeconds(2));
        updateCreatedDate(entityManager, thirdSavedMessage.getId(), base);
        entityManager.flush();

        entityManager.refresh(firstSavedMessage);
        entityManager.refresh(secondSavedMessage);
        entityManager.refresh(thirdSavedMessage);

        ChatMessageSliceDto actual = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), thirdSavedMessage.getId(), thirdSavedMessage.getCreatedDate(), 2
        );

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.content().get(0))
                        .isEqualTo(firstSavedMessage),
                () -> AssertionsForClassTypes.assertThat(actual.content().get(1))
                        .isEqualTo(secondSavedMessage)
        );

    }

    @Test
    @DisplayName("동일 시간일 경우 아이디의 내림차순으로 정렬된다")
    void getPreviousMessageSuccess6() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();
        ChatRoom chatRoom = saveChatRoom(festival);

        ChatMessage firstSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 1", "image url 1", chatRoom, userEntity)
        );

        ChatMessage secondSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 2", "image url 2", chatRoom, userEntity)
        );

        ChatMessage thirdSavedMessage = chatMessageRepository.save(
                new ChatMessage("test message 3", "image url 3", chatRoom, userEntity)
        );

        LocalDateTime base = LocalDateTime.now();
        updateCreatedDate(entityManager, firstSavedMessage.getId(), base.minusSeconds(1));
        updateCreatedDate(entityManager, secondSavedMessage.getId(), base.minusSeconds(1));
        updateCreatedDate(entityManager, thirdSavedMessage.getId(), base);
        entityManager.flush();

        entityManager.refresh(firstSavedMessage);
        entityManager.refresh(secondSavedMessage);
        entityManager.refresh(thirdSavedMessage);

        ChatMessageSliceDto actual = qChatMessageRepository.findByChatRoomIdWithUser(
                chatRoom.getId(), thirdSavedMessage.getId(), thirdSavedMessage.getCreatedDate(), 2
        );

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.content().get(0))
                        .isEqualTo(secondSavedMessage),
                () -> AssertionsForClassTypes.assertThat(actual.content().get(1))
                        .isEqualTo(firstSavedMessage)
        );

    }
}
