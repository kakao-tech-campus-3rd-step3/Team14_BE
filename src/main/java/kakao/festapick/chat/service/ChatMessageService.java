package kakao.festapick.chat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.ChatRequestDto;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
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
    private final FileService fileService;
    private final TemporalFileRepository temporalFileRepository;

    // 채팅 메시지 보내기
    public void sendChat(Long chatRoomId, ChatRequestDto requestDto, Long userId) {
        UserEntity sender = userLowService.findById(userId);
        String senderName = sender.getUsername();
        String profileImgUrl = sender.getProfileImageUrl();

        ChatRoom chatRoom = chatRoomLowService.findByRoomId(chatRoomId);

        ChatMessage chatMessage = new ChatMessage(requestDto.content(), chatRoom, sender);
        // db에 저장 후
        ChatMessage savedMessage = chatMessageLowService.save(chatMessage);
        String url = saveFile(requestDto.imageInfo(), savedMessage.getId());
        ChatPayload payload = new ChatPayload(savedMessage.getId(), senderName,
                profileImgUrl, requestDto.content(), url);

        // 마지막에 웹소켓 전송
        webSocket.convertAndSend("/sub/" + chatRoom.getId() + "/messages", payload);
    }

    // 채팅방에서 최근 메시지 조회 (Pageable)
    public Page<ChatPayload> getPreviousMessages(Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> previousMessages = chatMessageLowService.findByChatRoomId(chatRoomId,
                pageable);

        List<FileEntity> files = findFilesByChatMessages(previousMessages);

        HashMap<Long, String> imageUrls = new HashMap<>();
        chatMessageIdAndUrlMapping(files, imageUrls);

        return previousMessages.map(chatMessage -> new ChatPayload(chatMessage,
                imageUrls.getOrDefault(chatMessage.getId(), null)));
    }

    // 유저가 작성한 메시지 전체 삭제 기능
    public void deleteChatMessagesByUserId(Long userId) {
        List<Long> chatMessageIds = chatMessageLowService.findAllByUserId(userId)
                .stream().map(ChatMessage::getId).toList();

        chatMessageLowService.deleteByUserId(userId);
        fileService.deleteByDomainIds(chatMessageIds,
                DomainType.CHAT); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }

    // 파일 저장
    private String saveFile(FileUploadRequest imageInfo, Long id) {
        if (imageInfo != null) {
            FileEntity file = new FileEntity(imageInfo.presignedUrl(), FileType.IMAGE, DomainType.CHAT, id);
            Long temporalFileId = imageInfo.id();
            fileService.saveAll(List.of(file));
            // 저장이 정상적으로 됬을 경우 임시 파일 목록에서 제거
            temporalFileRepository.deleteByIds(List.of(temporalFileId));
            return file.getUrl();
        }
        return null;
    }

    // 메시지들의 해당하는 파일 엔티티 찾기
    private List<FileEntity> findFilesByChatMessages(Page<ChatMessage> chatMessages) {
        List<Long> domainIds = chatMessages.stream()
                .map(ChatMessage::getId)
                .toList();

        return fileService.findAllFileEntityByDomain(domainIds, DomainType.CHAT);
    }

    // 채팅 메시지 엔티티와 url 매핑
    private void chatMessageIdAndUrlMapping(List<FileEntity> files,
            HashMap<Long, String> imageUrls) {
        files.forEach(file -> {
            imageUrls.put(file.getDomainId(), file.getUrl());
        });
    }
}
