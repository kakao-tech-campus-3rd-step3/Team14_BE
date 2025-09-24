package kakao.festapick.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
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
public class ChatRoomRepositoryTest {

    private final TestUtil testUtil = new TestUtil();
    private final FestivalRequestDto requestDto = new FestivalRequestDto("1234567", "test festival",
            11, "test addr1", "test addr2", "http://asd.test.com/example.jpg",
            testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));

    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private FestivalRepository festivalRepository;

    private Festival saveFestival() throws Exception {
        return festivalRepository.save(
                new Festival(requestDto, testUtil.createTourDetailResponse()));
    }

    @Test
    @DisplayName("채팅방 등록 성공 테스트")
    void createChatRoomSuccess() throws Exception {

        Festival festival = saveFestival();

        ChatRoom actual = chatRoomRepository.save(new ChatRoom("test room", festival));

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getRoomName()).isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival())
                        .isEqualTo(festival)
        );
    }

    @Test
    @DisplayName("축제 아이디로 채팅방 조회 성공 테스트")
    void getChatRoomSuccess() throws Exception {

        Festival festival = saveFestival();
        chatRoomRepository.save(new ChatRoom("test room", festival));

        Optional<ChatRoom> find = chatRoomRepository.findByFestivalId(festival.getId());

        assertThat(find).isPresent();

        ChatRoom actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getRoomName()).isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival())
                        .isEqualTo(festival)
        );
    }

    @Test
    @DisplayName("채팅방 아이디로 채팅방 조회 성공 테스트")
    void getChatRoomSuccess2() throws Exception {

        Festival festival = saveFestival();
        ChatRoom saved = chatRoomRepository.save(new ChatRoom("test room", festival));

        Optional<ChatRoom> find = chatRoomRepository.findByRoomId(saved.getId());

        assertThat(find).isPresent();

        ChatRoom actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getRoomName()).isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival())
                        .isEqualTo(festival)
        );
    }
}
