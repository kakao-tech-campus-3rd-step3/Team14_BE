package kakao.festapick.redis.redis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatParticipant;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRequestDto;
import kakao.festapick.chat.dto.UnreadEventPayload;
import kakao.festapick.chat.service.ChatMessageLowService;
import kakao.festapick.chat.service.ChatParticipantLowService;
import kakao.festapick.chat.service.ChatRoomLowService;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.redis.service.RedisPubSubService;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
public class RedisPubSubServiceTest {

    private final TestUtil testUtil = new TestUtil();
    @InjectMocks
    private RedisPubSubService redisPubSubService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SimpMessagingTemplate webSocket;
    @Mock
    private UserLowService userLowService;
    @Mock
    private ChatMessageLowService chatMessageLowService;
    @Mock
    private ChatParticipantLowService chatParticipantLowService;
    @Mock
    private ChatRoomLowService chatRoomLowService;
    @Mock
    private TemporalFileRepository temporalFileRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("채팅 메시지 레디스로 전송 성공")
    void sendChatMessageToRedisSuccess() throws NoSuchFieldException, IllegalAccessException {
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testFestival();
        ChatRoom chatRoom = new ChatRoom("test room", festival);
        ChatMessage chatMessage = new ChatMessage("test message", "image url",chatRoom, user);
        ChatParticipant chatParticipant = new ChatParticipant(user, chatRoom);

        given(userLowService.getReferenceById(any()))
                .willReturn(user);
        given(chatRoomLowService.findByRoomId(any()))
                .willReturn(chatRoom);
        given(chatMessageLowService.save(any()))
                .willReturn(chatMessage);
        given(chatParticipantLowService.findByChatRoomIdAndUserId(any(), any()))
                .willReturn(chatParticipant);

        ChatRequestDto requestDto = new ChatRequestDto("test message", new FileUploadRequest(1L,"image"));
        redisPubSubService.sendChatMessageToRedis(chatRoom.getId(), requestDto, user.getId());

        verify(userLowService).getReferenceById(any());
        verify(chatRoomLowService).findByRoomId(any());
        verify(chatMessageLowService).save(any());
        verify(temporalFileRepository).deleteByIds(any());
        verify(redisTemplate, times(2)).convertAndSend((String) any(), (Object) any());
        verify(chatParticipantLowService).findByChatRoomId(any());
        verify(chatParticipantLowService).findByChatRoomIdAndUserId(any(), any());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatMessageLowService);
        verifyNoMoreInteractions(temporalFileRepository);
        verifyNoMoreInteractions(objectMapper);
        verifyNoMoreInteractions(webSocket);
        verifyNoMoreInteractions(redisTemplate);
        verifyNoMoreInteractions(chatParticipantLowService);
    }

    @Test
    @DisplayName("채팅 메시지 각 클라이언트로 전파 성공")
    void sendChatMessageToClientSuccess() throws JsonProcessingException {
        Long chatRoomId = 1L;
        Long chatMessageId = 1L;
        Long userId = 2L;
        String senderName = "testUser1";
        String profileImgUrl = "testProfileImg";
        String content = "test message";
        String imgUrl = "testImgUrl";
        String channel = "chat." + chatRoomId;
        String payload = """
                {
                    "id": %d,
                    "userId": %d,
                    "senderName": "%s",
                    "profileImgUrl": "%s",
                    "content": "%s",
                    "imgUrl": "%s"
                }
                """.formatted(chatMessageId, userId, senderName, profileImgUrl, content, imgUrl);

        ChatPayload chatPayload = new ChatPayload(chatMessageId, userId, senderName, profileImgUrl, content, imgUrl, LocalDateTime.now());

        Message message = new DefaultMessage(
                channel.getBytes(),
                payload.getBytes()
        );

        byte[] pattern = "".getBytes();

        given(objectMapper.readValue((String)any(), (Class<Object>) any()))
                .willReturn(chatPayload);

        redisPubSubService.onMessage(message, pattern);

        verify(objectMapper).readValue((String) any(), (Class<Object>) any());
        verify(webSocket).convertAndSend((String) any(), (Object) any());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatMessageLowService);
        verifyNoMoreInteractions(temporalFileRepository);
        verifyNoMoreInteractions(objectMapper);
        verifyNoMoreInteractions(webSocket);
        verifyNoMoreInteractions(redisTemplate);

    }

    @Test
    @DisplayName("채팅 알림 각 클라이언트로 전파 성공")
    void sendChatAlarmToClientSuccess() throws JsonProcessingException {
        Long chatRoomId = 1L;
        Long userId = 2L;
        String channel = "unreads";
        String payload = """
                {
                    "chatRoomId": %d,
                    "userIds": [%d]
                }
                """.formatted(chatRoomId, userId);

        UnreadEventPayload event = new UnreadEventPayload(chatRoomId, List.of(userId));

        Message message = new DefaultMessage(
                channel.getBytes(),
                payload.getBytes()
        );

        byte[] pattern = "".getBytes();

        given(objectMapper.readValue((String)any(), (Class<Object>) any()))
                .willReturn(event);

        redisPubSubService.onMessage(message, pattern);

        verify(objectMapper).readValue((String) any(), (Class<Object>) any());
        verify(webSocket).convertAndSendToUser((String) any(), (String) any(), (Object) any());
        verifyNoMoreInteractions(chatRoomLowService);
        verifyNoMoreInteractions(userLowService);
        verifyNoMoreInteractions(chatMessageLowService);
        verifyNoMoreInteractions(temporalFileRepository);
        verifyNoMoreInteractions(objectMapper);
        verifyNoMoreInteractions(webSocket);
        verifyNoMoreInteractions(redisTemplate);

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
