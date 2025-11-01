package kakao.festapick.chat.dto;

public record ChatRoomReadStatusDto(
        Long roomId,
        String roomName,
        Long festivalId,
        String posterInfo,
        Boolean existNewMessage
) {

}
