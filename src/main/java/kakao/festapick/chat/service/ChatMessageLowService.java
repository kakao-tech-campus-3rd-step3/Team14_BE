package kakao.festapick.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.dto.ChatMessageSliceDto;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.QChatMessageRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageLowService {

    private final ChatMessageRepository chatMessageRepository;
    private final QChatMessageRepository qChatMessageRepository;

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public ChatMessageSliceDto findByChatRoomIdWithUser(Long chatRoomId, Long cursorId, LocalDateTime cursorTime, int size) {
        return qChatMessageRepository.findByChatRoomIdWithUser(chatRoomId, cursorId, cursorTime, size);
    }

    public List<ChatMessage> findAllByChatRoomId(Long chatRoomId) {
        return chatMessageRepository.findAllByChatRoomId(chatRoomId);
    }

    public List<ChatMessage> findAllByUserId(Long userId) {
        return chatMessageRepository.findAllByUserId(userId);
    }

    public void deleteByChatRoomId(Long chatRoomId) {
        chatMessageRepository.deleteByChatRoomId(chatRoomId);
    }

    public void deleteByUserId(Long userId) {
        chatMessageRepository.deleteByUserId(userId);
    }

    public ChatMessage findById(Long id) {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.CHAT_MESSAGE_NOT_FOUND));
    }
}
