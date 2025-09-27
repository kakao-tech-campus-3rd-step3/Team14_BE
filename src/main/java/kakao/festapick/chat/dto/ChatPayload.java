package kakao.festapick.chat.dto;

public record ChatPayload(
        Long id,
        String senderName,
        String profileImgUrl,
        String content
) {

}
