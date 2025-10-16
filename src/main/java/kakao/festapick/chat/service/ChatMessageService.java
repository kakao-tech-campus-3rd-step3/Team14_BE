package kakao.festapick.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRequestDto;
import kakao.festapick.chat.dto.PreviousMessagesResponseDto;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageLowService chatMessageLowService;
    private final S3Service s3Service;

    // 채팅방에서 최근 메시지 조회
    public PreviousMessagesResponseDto getPreviousMessages(Long chatRoomId, int size, Long cursorId, LocalDateTime cursorTime) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<ChatMessage> prevMessageSlice = chatMessageLowService.findByChatRoomId(chatRoomId, cursorId, cursorTime, pageable);
        Boolean hasMoreList = prevMessageSlice.hasNext();
        List<ChatMessage> prevMessageList = new ArrayList<>(prevMessageSlice.getContent());
        // 프론트에 전달하기 위해 역전, 프론트에는 id 기준 오름 차순으로 전달
        Collections.reverse(prevMessageList);
        return new PreviousMessagesResponseDto(prevMessageList, hasMoreList);
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
