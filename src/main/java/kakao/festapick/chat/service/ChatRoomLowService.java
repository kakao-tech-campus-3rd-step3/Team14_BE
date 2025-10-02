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
public class ChatRoomLowService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public Optional<ChatRoom> findByFestivalId(Long festivalId) {
        return chatRoomRepository.findByFestivalId(festivalId);
    }

    public ChatRoom findByRoomId(Long roomId) {
        return chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.CHATROOM_NOT_FOUND));
    }

    public void deleteById(Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId);
    }

    public Page<ChatRoom> findAll(Pageable pageable) {
        return chatRoomRepository.findAll(pageable);
    }
}
