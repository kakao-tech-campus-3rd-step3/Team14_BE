package kakao.festapick.chat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
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

    private static void chatMessageIdAndUrlMapping(List<FileEntity> files,
            HashMap<Long, List<String>> imageUrls) {
        files.forEach(file -> {
            if (!imageUrls.containsKey(file.getDomainId())) {
                imageUrls.put(file.getDomainId(), new ArrayList<>());
            }
            imageUrls.get(file.getDomainId()).add(file.getUrl());
        });
    }

    public void sendChat(Long chatRoomId, SendChatRequestDto requestDto, Long userId) {
        UserEntity sender = userLowService.findById(userId);
        String senderName = sender.getUsername();
        String profileImgUrl = sender.getProfileImageUrl();

        ChatRoom chatRoom = chatRoomLowService.findByRoomId(chatRoomId);

        ChatMessage chatMessage = new ChatMessage(requestDto.content(), chatRoom, sender);
        ChatMessage savedMessage = chatMessageLowService.save(chatMessage);
        List<String> urls = saveFiles(requestDto.imageInfos(), savedMessage.getId());
        ChatPayload payload = new ChatPayload(savedMessage.getId(), senderName,
                profileImgUrl, requestDto.content(), urls);

        webSocket.convertAndSend("/sub/" + chatRoom.getId() + "/messages", payload);
    }

    public Page<ChatPayload> getPreviousMessages(Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> previousMessages = chatMessageLowService.findByChatRoomId(chatRoomId,
                pageable);

        List<FileEntity> files = findFilesByChatMessages(previousMessages);

        HashMap<Long, List<String>> imageUrls = new HashMap<>();
        chatMessageIdAndUrlMapping(files, imageUrls);

        return previousMessages.map(chatMessage -> new ChatPayload(chatMessage,
                imageUrls.getOrDefault(chatMessage.getId(), List.of())));
    }

    public void deleteChatMessagesByUserId(Long userId) {
        List<Long> chatMessageIds = chatMessageLowService.findAllByUserId(userId)
                .stream().map(ChatMessage::getId).toList();

        chatMessageLowService.deleteByUserId(userId);
        fileService.deleteByDomainIds(chatMessageIds, DomainType.CHAT); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }

    private List<String> saveFiles(List<FileUploadRequest> imageInfos, Long id) {
        List<FileEntity> files = new ArrayList<>();
        List<Long> temporalFileIds = new ArrayList<>();

        if (imageInfos != null) {
            imageInfos.forEach(imageInfo -> {
                files.add(new FileEntity(imageInfo.presignedUrl(), FileType.IMAGE, DomainType.CHAT, id));
                temporalFileIds.add(imageInfo.id());
            });
        }

        if (!files.isEmpty()) {
            fileService.saveAll(files);
        }
        temporalFileRepository.deleteByIds(temporalFileIds);

        return files.stream().map(FileEntity::getUrl).toList();
    }

    private List<FileEntity> findFilesByChatMessages(Page<ChatMessage> chatMessages) {
        List<Long> domainIds = chatMessages.stream()
                .map(ChatMessage::getId)
                .toList();

        return fileService.findAllFileEntityByDomain(domainIds, DomainType.CHAT);
    }
}
