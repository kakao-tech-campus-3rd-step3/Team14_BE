package kakao.festapick.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
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
import org.springframework.data.domain.Pageable;
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
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("채팅 전송 성공")
    void createChatMessageSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);
        ChatMessage chatMessage = new ChatMessage(1L, "test message", chatRoom, user);

        given(userLowService.findById(any()))
                .willReturn(user);
        given(chatRoomRepository.findById(any()))
                .willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any()))
                .willReturn(chatMessage);

        SendChatRequestDto requestDto = new SendChatRequestDto("test message");
        chatMessageService.sendChat(chatRoom.getId(), requestDto, user.getId());

        verify(userLowService).findById(any());
        verify(chatRoomRepository).findById(any());
        verify(chatMessageRepository).save(any());
        verify(webSocket).convertAndSend((String) any(), (Object) any());
        verifyNoMoreInteractions(chatRoomRepository);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatMessageRepository);
        verifyNoMoreInteractions(webSocket);
    }

    @Test
    @DisplayName("이전 채팅 내역 불러오기")
    void getPreviousMessagesSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom(1L, "test room", festival);
        ChatMessage chatMessage = new ChatMessage(1L, "test message", chatRoom, user);

        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(chatMessage);

        Page<ChatMessage> page = new PageImpl<>(messageList);

        given(chatMessageRepository.findByChatRoomId(any(), any()))
                .willReturn(page);

        Page<ChatPayload> response = chatMessageService.getPreviousMessages(1L,
                PageRequest.of(0, 1));

        assertAll(
                () -> AssertionsForClassTypes.assertThat(response.getContent().get(0)).isNotNull()
        );

        verify(chatMessageRepository).findByChatRoomId(any(), any());
        verifyNoMoreInteractions(chatRoomRepository);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatMessageRepository);
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
