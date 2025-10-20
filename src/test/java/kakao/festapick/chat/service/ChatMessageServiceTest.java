package kakao.festapick.chat.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatMessageSliceDto;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.PreviousMessagesResponseDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    private final TestUtil testUtil = new TestUtil();
    @InjectMocks
    private ChatMessageService chatMessageService;
    @Mock
    private SimpMessagingTemplate webSocket;
    @Mock
    private UserLowService userLowService;
    @Mock
    private ChatMessageLowService chatMessageLowService;
    @Mock
    private ChatRoomLowService chatRoomLowService;


    @Test
    @DisplayName("이전 채팅 내역 불러오기")
    void getPreviousMessagesSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);

        List<ChatMessage> messageList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ChatMessage chatMessage = new ChatMessage("test message" + i, "image url" + i,
                    chatRoom, user);
            messageList.add(chatMessage);
        }

        List<ChatMessage> reversedMessageList = new ArrayList<>(messageList.reversed());
        ChatMessageSliceDto slice = new ChatMessageSliceDto(reversedMessageList, true);

        given(chatMessageLowService.findByChatRoomId(any(), any(), any(), anyInt()))
                .willReturn(slice);

        PreviousMessagesResponseDto response = chatMessageService.getPreviousMessages(1L, 1, null, null);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(response.content())
                        .isEqualTo(messageList.stream().map(ChatPayload::new).toList())
        );

        verify(chatMessageLowService).findByChatRoomId(any(), any(),any(), anyInt());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatMessageLowService);
        verifyNoMoreInteractions(webSocket);
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
