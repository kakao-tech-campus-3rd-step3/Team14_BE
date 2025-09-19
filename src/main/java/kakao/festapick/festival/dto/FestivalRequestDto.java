package kakao.festapick.festival.dto;

import java.time.LocalDate;

public record FestivalRequestDto(
        String contentId,
        String title,
        int areaCode,
        String addr1,
        String addr2,
        String posterInfo,
        LocalDate startDate,
        LocalDate endDate
){ }
