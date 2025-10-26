package kakao.festapick.chat.dto;

import java.util.List;

public record UnreadEventPayload(
        Long chatRoomId,
        List<Long> userIds
) {

}
