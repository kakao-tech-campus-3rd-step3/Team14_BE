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
public class ChatRoomServiceTest {

    private final TestUtil testUtil = new TestUtil();
    @InjectMocks
    private ChatRoomService chatRoomService;
    @Mock
    private ChatRoomLowService chatRoomLowService;
    @Mock
    private FestivalLowService festivalLowService;

    @Test
    @DisplayName("축제 아이디로 존재하는 채팅방 조회 성공")
    void getExistChatRoomSuccess() throws NoSuchFieldException, IllegalAccessException {
        Festival festival = testFestival();

        ChatRoom chatRoom = new ChatRoom("test room", festival);

        given(chatRoomLowService.findByFestivalId(any()))
                .willReturn(Optional.of(chatRoom));

        ChatRoomResponseDto responseDto = chatRoomService.getExistChatRoomOrMakeByFestivalId(
                festival.getId());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.roomName())
                        .isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalId())
                        .isEqualTo(festival.getId())
        );

        verify(chatRoomLowService).findByFestivalId(any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(chatRoomLowService);
    }

    @Test
    @DisplayName("축제 아이디로 채팅방 생성 성공")
    void createChatRoomSuccess() throws NoSuchFieldException, IllegalAccessException {
        Festival festival = testFestival();

        ChatRoom chatRoom = new ChatRoom("test room", festival);

        given(chatRoomLowService.findByFestivalId(any()))
                .willReturn(Optional.empty());
        given(festivalLowService.findFestivalById(any()))
                .willReturn(festival);
        given(chatRoomLowService.save(any()))
                .willReturn(chatRoom);

        ChatRoomResponseDto responseDto = chatRoomService.getExistChatRoomOrMakeByFestivalId(
                festival.getId());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.roomName())
                        .isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalId())
                        .isEqualTo(festival.getId())
        );

        verify(festivalLowService).findFestivalById(any());
        verify(chatRoomLowService).findByFestivalId(any());
        verify(chatRoomLowService).save(any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(chatRoomLowService);
    }

    @Test
    @DisplayName("방 아이디로 존재하는 채팅방 조회 성공")
    void getExistChatRoomSuccess2() throws NoSuchFieldException, IllegalAccessException {
        Festival festival = testFestival();

        ChatRoom chatRoom = new ChatRoom("test room", festival);

        given(chatRoomLowService.findByRoomId(any()))
                .willReturn(chatRoom);

        ChatRoomResponseDto responseDto = chatRoomService.getChatRoomByRoomId(festival.getId());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(responseDto.roomName())
                        .isEqualTo("test room"),
                () -> AssertionsForClassTypes.assertThat(responseDto.festivalId())
                        .isEqualTo(festival.getId())
        );

        verify(chatRoomLowService).findByRoomId(any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(chatRoomLowService);
    }

    @Test
    @DisplayName("없는 방 아이디로 채팅방 조회 실패")
    void getExistChatRoomFail() {

        given(chatRoomLowService.findByRoomId(any()))
                .willThrow(new NotFoundEntityException(ExceptionCode.CHATROOM_NOT_FOUND));

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> chatRoomService.getChatRoomByRoomId(999L));

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.CHATROOM_NOT_FOUND);

        verify(chatRoomLowService).findByRoomId(any());
        verifyNoMoreInteractions(festivalLowService);
        verifyNoMoreInteractions(chatRoomLowService);
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
