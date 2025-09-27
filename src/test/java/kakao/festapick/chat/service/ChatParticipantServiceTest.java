package kakao.festapick.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.Optional;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
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
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
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

        given(chatRoomRepository.findById(any()))
                .willReturn(Optional.of(chatRoom));
        given(userLowService.findById(any()))
                .willReturn(user);
        given(chatParticipantRepository.existsByUserAndChatRoom(any(), any()))
                .willReturn(false);
        given(chatParticipantRepository.save(any()))
                .willReturn(chatParticipant);

        chatParticipantService.enterChatRoom(user.getId(), chatRoom.getId());

        verify(chatRoomRepository).findById(any());
        verify(userLowService).findById(any());
        verify(chatParticipantRepository).existsByUserAndChatRoom(any(),any());
        verify(chatParticipantRepository).save(any());
        verifyNoMoreInteractions(chatRoomRepository);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatParticipantRepository);
    }

    @Test
    @DisplayName("채팅방 재입장 성공")
    void enterChatRoomSuccess2() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);

        given(chatRoomRepository.findById(any()))
                .willReturn(Optional.of(chatRoom));
        given(userLowService.findById(any()))
                .willReturn(user);
        given(chatParticipantRepository.existsByUserAndChatRoom(any(), any()))
                .willReturn(true);

        chatParticipantService.enterChatRoom(user.getId(), chatRoom.getId());

        verify(chatRoomRepository).findById(any());
        verify(userLowService).findById(any());
        verify(chatParticipantRepository).existsByUserAndChatRoom(any(),any());
        verifyNoMoreInteractions(chatRoomRepository);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatParticipantRepository);
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
