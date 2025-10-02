package kakao.festapick.chat.service;

import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final SimpMessagingTemplate webSocket;
    private final UserLowService userLowService;
    private final ChatMessageLowService chatMessageLowService;
    private final ChatRoomLowService chatRoomLowService;

    public void sendChat(Long chatRoomId, SendChatRequestDto requestDto, Long userId) {
        UserEntity sender = userLowService.findById(userId);
        String senderName = sender.getUsername();
        String profileImgUrl = sender.getProfileImageUrl();

        ChatRoom chatRoom = chatRoomLowService.findByRoomId(chatRoomId);

        ChatMessage chatMessage = new ChatMessage(requestDto.content(), chatRoom, sender);
        ChatMessage savedMessage = chatMessageLowService.save(chatMessage);
        ChatPayload payload = new ChatPayload(savedMessage.getId(), senderName,
                profileImgUrl, requestDto.content());

        webSocket.convertAndSend("/sub/" + chatRoom.getId() + "/messages", payload);
    }

    public Page<ChatPayload> getPreviousMessages(Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> previousMessages = chatMessageLowService.findByChatRoomId(chatRoomId,
                pageable);
        return previousMessages.map(chatMessage -> new ChatPayload(chatMessage.getId(),
                chatMessage.getSenderName(),
                chatMessage.getSenderProfileUrl(), chatMessage.getContent()));
    }
}
