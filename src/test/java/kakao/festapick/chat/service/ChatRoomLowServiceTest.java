package kakao.festapick.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.Optional;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.repository.ChatRoomRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatRoomLowServiceTest {

    private final TestUtil testUtil = new TestUtil();
    @InjectMocks
    private ChatRoomLowService chatRoomLowService;
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("방 아이디로 존재하는 채팅방 조회 성공")
    void getExistChatRoomSuccess2() throws NoSuchFieldException, IllegalAccessException {
        Festival festival = testFestival();

        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);

        given(chatRoomRepository.findByRoomId(any()))
                .willReturn(Optional.of(chatRoom));

        ChatRoom response = chatRoomLowService.findByRoomId(chatRoom.getId());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(response.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(response.getRoomName())
                        .isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(response.getFestival())
                        .isEqualTo(festival)
        );

        verify(chatRoomRepository).findByRoomId(any());
        verifyNoMoreInteractions(chatRoomRepository);
    }

    @Test
    @DisplayName("없는 방 아이디로 채팅방 조회 실패")
    void getExistChatRoomFail() {

        given(chatRoomRepository.findByRoomId(any()))
                .willReturn(Optional.empty());

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> chatRoomLowService.findByRoomId(999L));

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.CHATROOM_NOT_FOUND);

        verify(chatRoomRepository).findByRoomId(any());
        verifyNoMoreInteractions(chatRoomRepository);
    }

    private Festival testFestival() throws NoSuchFieldException, IllegalAccessException {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg",
                testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, new TourDetailResponse());

        Field idField = Festival.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(festival, 1L);

        return festival;
    }
}
