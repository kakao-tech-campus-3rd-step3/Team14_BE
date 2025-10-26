package kakao.festapick.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.List;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomReadStatusDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class ChatParticipantServiceTest {

    private final TestUtil testUtil = new TestUtil();
    @InjectMocks
    private ChatParticipantService chatParticipantService;
    @Mock
    private ChatParticipantLowService chatParticipantLowService;
    @Mock
    private ChatRoomLowService chatRoomLowService;
    @Mock
    private UserLowService userLowService;

    @Test
    @DisplayName("채팅방 입장 성공")
    void enterChatRoomSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);

        ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);

        given(chatRoomLowService.findByRoomId(any()))
                .willReturn(chatRoom);
        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(chatParticipantLowService.existsByUserAndChatRoom(any(), any()))
                .willReturn(false);
        given(chatParticipantLowService.save(any()))
                .willReturn(chatParticipant);

        ChatParticipant actual = chatParticipantService.enterChatRoom(user.getId(), chatRoom.getId());

        assertSoftly(softly -> {
                    softly.assertThat(actual).isEqualTo(chatParticipant);
                }
        );

        verify(chatRoomLowService).findByRoomId(any());
        verify(userLowService).getReferenceById(any());
        verify(chatParticipantLowService).existsByUserAndChatRoom(any(), any());
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
        ChatRoom chatRoom = new ChatRoom("test room", festival);

        ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);

        given(chatRoomLowService.findByRoomId(any()))
                .willReturn(chatRoom);
        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(chatParticipantLowService.existsByUserAndChatRoom(any(), any()))
                .willReturn(true);
        given(chatParticipantLowService.findByChatRoomIdAndUserIdWithChatRoom(any(), any()))
                .willReturn(chatParticipant);

        ChatParticipant actual = chatParticipantService.enterChatRoom(user.getId(), chatRoom.getId());

        assertSoftly(softly -> {
                    softly.assertThat(actual).isEqualTo(chatParticipant);
                }
        );

        verify(chatRoomLowService).findByRoomId(any());
        verify(userLowService).getReferenceById(any());
        verify(chatParticipantLowService).existsByUserAndChatRoom(any(), any());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatParticipantLowService);
    }

    @Test
    @DisplayName("내 채팅방 정보 조회 성공")
    void getMyChatRoomSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);
        ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);
        Page<ChatParticipant> chatParticipantPage = new PageImpl<ChatParticipant>(
                List.of(chatParticipant), PageRequest.of(0, 1), 1);

        given(chatParticipantLowService.findByUserIdWithChatRoomAndFestival(any(), any()))
                .willReturn(chatParticipantPage);

        Page<ChatRoomReadStatusDto> resultPage = chatParticipantService.getMyChatRoomsReadStatus(
                user.getId(), PageRequest.of(0, 1));

        ChatRoomReadStatusDto actual = resultPage.getContent().get(0);

        assertSoftly(softly -> {
                    softly.assertThat(actual.roomName()).isEqualTo(chatRoom.getRoomName());
                    softly.assertThat(actual.posterInfo()).isEqualTo(festival.getPosterInfo());
                    softly.assertThat(actual.existNewMessage()).isEqualTo(false);
                }
        );

        verify(chatParticipantLowService).findByUserIdWithChatRoomAndFestival(any(), any());
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
