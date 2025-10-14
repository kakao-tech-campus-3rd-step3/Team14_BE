package kakao.festapick.chat.service;

import java.util.List;
import java.util.Objects;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.fileupload.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageLowService chatMessageLowService;
    private final S3Service s3Service;

    // 채팅방에서 최근 메시지 조회 (Pageable)
    public Page<ChatPayload> getPreviousMessages(Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> previousMessages = chatMessageLowService.findByChatRoomId(chatRoomId,
                pageable);
        return previousMessages.map(ChatPayload::new);
    }

    // 유저가 작성한 메시지 전체 삭제 기능
    public void deleteChatMessagesByUserId(Long userId) {
        List<String> chatMessageImageUrls = chatMessageLowService
                .findAllByUserId(userId)
                .stream()
                .map(ChatMessage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();

        chatMessageLowService.deleteByUserId(userId);
        s3Service.deleteFiles(chatMessageImageUrls); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }
}
