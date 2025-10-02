package kakao.festapick.chat.service;

import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
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
public class ChatMessageLowService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public Page<ChatMessage> findByChatRoomId(Long chatRoomId, Pageable pageable) {
        return chatMessageRepository.findByChatRoomId(chatRoomId, pageable);
    }

    public List<ChatMessage> findAllByChatRoomId(Long chatRoomId) {
        return chatMessageRepository.findAllByChatRoomId(chatRoomId);
    }

    public List<ChatMessage> findAllByChatRoomIdAndUserId(Long chatRoomId, Long userId) {
        return chatMessageRepository.findAllByChatRoomIdAndUserId(chatRoomId, userId);
    }

    public void deleteByChatRoomId(Long chatRoomId) {
        chatMessageRepository.deleteByChatRoomId(chatRoomId);
    }

    public void deleteByUserId(Long userId) {
        chatMessageRepository.deleteByUserId(userId);
    }
}
