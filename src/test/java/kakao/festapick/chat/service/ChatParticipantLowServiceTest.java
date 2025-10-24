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
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
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
public class ChatParticipantLowServiceTest {

    private final TestUtil testUtil = new TestUtil();
    @InjectMocks
    private ChatParticipantLowService chatParticipantLowService;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Test
    @DisplayName("방 아이디와 유저 아이디로 존재하는 채팅 참여자 조회 성공")
    void getExistChatParticipantSuccess() throws NoSuchFieldException, IllegalAccessException {
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);
        UserEntity userEntity = testUtil.createTestUser();

        ChatParticipant chatParticipant = new ChatParticipant(userEntity, chatRoom);

        given(chatParticipantRepository.findByChatRoomIdAndUserIdWithChatRoom(any(), any()))
                .willReturn(Optional.of(chatParticipant));

        ChatParticipant response = chatParticipantLowService.findByChatRoomIdAndUserIdWithChatRoom(chatRoom.getId(), userEntity.getId());

        assertAll(
                () -> AssertionsForClassTypes.assertThat(response.getUser())
                        .isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(response.getChatRoom())
                        .isEqualTo(chatRoom),
                () -> AssertionsForClassTypes.assertThat(response.getMessageSeq())
                        .isEqualTo(chatRoom.getMessageSeq())
        );

        verify(chatParticipantRepository).findByChatRoomIdAndUserIdWithChatRoom(any(), any());
        verifyNoMoreInteractions(chatParticipantRepository);
    }

    @Test
    @DisplayName("없는 방 아이디로 채팅 참여자 조회 실패")
    void getExistChatParticipantFail() {
        UserEntity userEntity = testUtil.createTestUser();
        given(chatParticipantRepository.findByChatRoomIdAndUserIdWithChatRoom(any(), any()))
                .willReturn(Optional.empty());

        NotFoundEntityException e = Assertions.assertThrows(NotFoundEntityException.class,
                () -> chatParticipantLowService.findByChatRoomIdAndUserIdWithChatRoom(999L, userEntity.getId()));

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.CHAT_PARTICIPANT_NOT_FOUND);

        verify(chatParticipantRepository).findByChatRoomIdAndUserIdWithChatRoom(any(), any());
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
