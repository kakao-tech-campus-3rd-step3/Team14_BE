package kakao.festapick.chat.service;

import java.util.Optional;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.chat.dto.ChatRoomResponseDto;
import kakao.festapick.chat.repository.ChatMessageRepository;
import kakao.festapick.chat.repository.ChatParticipantRepository;
import kakao.festapick.chat.repository.ChatRoomRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final FestivalRepository festivalRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomResponseDto getExistChatRoomOrMakeByFestivalId(Long festivalId) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByFestivalId(festivalId);
        if (chatRoomOptional.isEmpty()) {
            Festival festival = festivalRepository.findFestivalById(festivalId)
                    .orElseThrow(
                            () -> new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));
            ChatRoom newChatRoom = new ChatRoom(festival.getTitle() + " 채팅방", festival);
            ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
            return new ChatRoomResponseDto(savedChatRoom.getId(),
                    savedChatRoom.getRoomName(), savedChatRoom.getFestivalId());
        } else {
            ChatRoom findChatRoom = chatRoomOptional.get();
            return new ChatRoomResponseDto(findChatRoom.getId(),
                    findChatRoom.getRoomName(), findChatRoom.getFestivalId());
        }
    }

    public ChatRoomResponseDto getChatRoomByRoomId(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.CHATROOM_NOT_FOUND));
        return new ChatRoomResponseDto(chatRoom.getId(), chatRoom.getRoomName(),
                chatRoom.getFestivalId());
    }

    public Page<ChatRoomResponseDto> getChatRooms(Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomRepository.findAll(pageable);
        return chatRooms.map(chatRoom -> new ChatRoomResponseDto(chatRoom.getId(),
                chatRoom.getRoomName(), chatRoom.getFestivalId()));
    }

    public void deleteChatRoomByfestivalIdIfExist(Long festivalId) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByFestivalId(festivalId);
        if (chatRoom.isPresent()) {
            Long chatRoomId = chatRoom.get().getId();
            deleteRelatedEntity(chatRoomId);
            chatRoomRepository.deleteById(chatRoomId);
        }
    }

    private void deleteRelatedEntity(Long chatRoomId) {
        chatParticipantRepository.deleteByChatRoomId(chatRoomId);
        chatMessageRepository.deleteByChatRoomId(chatRoomId);
    }
}
