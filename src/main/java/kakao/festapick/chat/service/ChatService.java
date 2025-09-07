package kakao.festapick.chat.service;

import kakao.festapick.chat.domain.Chat;
import kakao.festapick.chat.dto.ChatPayload;
import kakao.festapick.chat.dto.SendChatRequestDto;
import kakao.festapick.chat.repository.ChatRepository;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.service.FestivalService;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.OAuth2UserService;
import kakao.festapick.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final SimpMessagingTemplate websocket;
    private final OAuth2UserService userService;
    private final FestivalRepository festivalRepository;
    private final WishRepository wishRepository;
    private final ChatRepository chatRepository;

    public void sendChat(Long festivalId, SendChatRequestDto requestDto, String identifier) {

        UserEntity sender = userService.findByIdentifier(identifier);
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() ->new NotFoundEntityException(ExceptionCode.FESTIVAL_NOT_FOUND));

        wishRepository.findByUserIdentifierAndFestivalId(identifier, festivalId)
                .orElseThrow(() ->new NotFoundEntityException(ExceptionCode.WISH_NOT_FOUND));

        String senderName = sender.getUsername();

        ChatPayload payload = new ChatPayload(festivalId, senderName, requestDto.content(),
                requestDto.sendDateTime());
        Chat chat = new Chat(sender,festival, requestDto.content(), requestDto.sendDateTime());
        chatRepository.save(chat);

        websocket.convertAndSend("/sub/festival/" + festivalId, payload);

    }
}
