package kakao.festapick.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatParticipantServiceTest {

    @InjectMocks
    private ChatParticipantService chatParticipantService;
    @Mock
    private ChatParticipantLowService chatParticipantLowService;
    @Mock
    private ChatRoomLowService chatRoomLowService;
    @Mock
    private UserLowService userLowService;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("채팅방 입장 성공")
    void enterChatRoomSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);

        ChatParticipant chatParticipant = new ChatParticipant(1L, user, chatRoom);

        given(chatRoomLowService.findByRoomId(any()))
                .willReturn(chatRoom);
        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(chatParticipantLowService.existsByUserAndChatRoom(any(), any()))
                .willReturn(false);
        given(chatParticipantLowService.save(any()))
                .willReturn(chatParticipant);

        chatParticipantService.enterChatRoom(user.getId(), chatRoom.getId());

        verify(chatRoomLowService).findByRoomId(any());
        verify(userLowService).getReferenceById(any());
        verify(chatParticipantLowService).existsByUserAndChatRoom(any(),any());
        verify(chatParticipantLowService).save(any());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatParticipantLowService);
    }

    @Test
    @DisplayName("채팅방 재입장 성공")
    void enterChatRoomSuccess2() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);

        given(chatRoomLowService.findByRoomId(any()))
                .willReturn(chatRoom);
        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(chatParticipantLowService.existsByUserAndChatRoom(any(), any()))
                .willReturn(true);

        chatParticipantService.enterChatRoom(user.getId(), chatRoom.getId());

        verify(chatRoomLowService).findByRoomId(any());
        verify(userLowService).getReferenceById(any());
        verify(chatParticipantLowService).existsByUserAndChatRoom(any(),any());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatParticipantLowService);
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
