package kakao.festapick.chat.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.fileupload.service.S3Service;
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
    private final S3Service s3Service;

    // 축제에 해당하는 채팅방이 존재하는지 확인 후 채팅방 정보 반환, 없으면 생성 후 반환
    public ChatRoomResponseDto getExistChatRoomOrMakeByFestivalId(Long festivalId) {
        Optional<ChatRoom> chatRoomOptional = chatRoomLowService.findByFestivalId(festivalId);
        // 조회 결과 채팅방이 없으면 축제에 해당하는 채팅방 생성후 반환
        if (chatRoomOptional.isEmpty()) {
            Festival festival = festivalLowService.findFestivalById(festivalId);
            ChatRoom newChatRoom = new ChatRoom(festival.getTitle() + " 채팅방", festival);
            ChatRoom savedChatRoom = chatRoomLowService.save(newChatRoom);
            return new ChatRoomResponseDto(savedChatRoom.getId(),
                    savedChatRoom.getRoomName(), savedChatRoom.getFestivalId());
        }
        // 있으면 반환
        else {
            ChatRoom findChatRoom = chatRoomOptional.get();
            return new ChatRoomResponseDto(findChatRoom.getId(),
                    findChatRoom.getRoomName(), findChatRoom.getFestivalId());
        }
    }

    // 채팅방 id로 채팅방 정보 조회
    public ChatRoomResponseDto getChatRoomByRoomId(Long roomId) {
        ChatRoom chatRoom = chatRoomLowService.findByRoomId(roomId);
        return new ChatRoomResponseDto(chatRoom.getId(), chatRoom.getRoomName(),
                chatRoom.getFestivalId());
    }

    // 현재 존재하는 채팅방 정보 목록 조회 (pageable)
    public Page<ChatRoomResponseDto> getChatRooms(Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomLowService.findAll(pageable);
        return chatRooms.map(chatRoom -> new ChatRoomResponseDto(chatRoom.getId(),
                chatRoom.getRoomName(), chatRoom.getFestivalId()));
    }

    // 축제 id로 해당 채팅방 삭제
    public void deleteChatRoomByfestivalIdIfExist(Long festivalId) {
        Optional<ChatRoom> chatRoom = chatRoomLowService.findByFestivalId(festivalId);
        // 조회 결과 채팅방이 있으면 연관 엔티티 제거 후 채팅방 삭제
        if (chatRoom.isPresent()) {
            Long chatRoomId = chatRoom.get().getId();
            deleteRelatedEntity(chatRoomId);
            chatRoomLowService.deleteById(chatRoomId);
        }
    }

    // 연관 엔티티 (채팅방 내 채팅 메시지, 채팅방 참여자) 정리
    private void deleteRelatedEntity(Long chatRoomId) {
        chatParticipantLowService.deleteByChatRoomId(chatRoomId);
        chatMessageLowService.deleteByChatRoomId(chatRoomId);

        List<String> chatMessageUrls = chatMessageLowService.findAllByChatRoomId(chatRoomId)
                .stream()
                .map(ChatMessage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();

        s3Service.deleteFiles(chatMessageUrls); // s3 파일 삭제를 동반하기 때문에 마지막에 호출
    }
}
