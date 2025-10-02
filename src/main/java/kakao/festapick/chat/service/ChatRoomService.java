package kakao.festapick.chat.service;

import java.util.List;
import java.util.Optional;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomLowService chatRoomLowService;
    private final FestivalLowService festivalLowService;
    private final ChatParticipantLowService chatParticipantLowService;
    private final ChatMessageLowService chatMessageLowService;
    private final FileService fileService;

    public ChatRoomResponseDto getExistChatRoomOrMakeByFestivalId(Long festivalId) {
        Optional<ChatRoom> chatRoomOptional = chatRoomLowService.findByFestivalId(festivalId);
        if (chatRoomOptional.isEmpty()) {
            Festival festival = festivalLowService.findFestivalById(festivalId);
            ChatRoom newChatRoom = new ChatRoom(festival.getTitle() + " 채팅방", festival);
            ChatRoom savedChatRoom = chatRoomLowService.save(newChatRoom);
            return new ChatRoomResponseDto(savedChatRoom.getId(),
                    savedChatRoom.getRoomName(), savedChatRoom.getFestivalId());
        } else {
            ChatRoom findChatRoom = chatRoomOptional.get();
            return new ChatRoomResponseDto(findChatRoom.getId(),
                    findChatRoom.getRoomName(), findChatRoom.getFestivalId());
        }
    }

    public ChatRoomResponseDto getChatRoomByRoomId(Long roomId) {
        ChatRoom chatRoom = chatRoomLowService.findByRoomId(roomId);
        return new ChatRoomResponseDto(chatRoom.getId(), chatRoom.getRoomName(),
                chatRoom.getFestivalId());
    }

    public Page<ChatRoomResponseDto> getChatRooms(Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomLowService.findAll(pageable);
        return chatRooms.map(chatRoom -> new ChatRoomResponseDto(chatRoom.getId(),
                chatRoom.getRoomName(), chatRoom.getFestivalId()));
    }

    public void deleteChatRoomByfestivalIdIfExist(Long festivalId) {
        Optional<ChatRoom> chatRoom = chatRoomLowService.findByFestivalId(festivalId);
        if (chatRoom.isPresent()) {
            Long chatRoomId = chatRoom.get().getId();
            deleteRelatedEntity(chatRoomId);
            chatRoomLowService.deleteById(chatRoomId);
        }
    }

    private void deleteRelatedEntity(Long chatRoomId) {
        chatParticipantLowService.deleteByChatRoomId(chatRoomId);
        chatMessageLowService.deleteByChatRoomId(chatRoomId);

        List<Long> chatMessageIds = chatMessageLowService.findAllByChatRoomId(chatRoomId)
                .stream().map(ChatMessage::getId).toList();
        fileService.deleteByDomainIds(chatMessageIds, DomainType.CHAT); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }
}
