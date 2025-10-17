package kakao.festapick.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.QChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    public Slice<ChatMessage> findByChatRoomId(Long chatRoomId, Long cursorId, LocalDateTime cursorTime, Pageable pageable) {
        return qChatMessageRepository.findByChatRoomId(chatRoomId, cursorId, cursorTime, pageable);
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
}
