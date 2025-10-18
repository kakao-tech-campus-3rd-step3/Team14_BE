package kakao.festapick.festivalnotice.dto;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.festivalnotice.domain.FestivalNotice;

public record FestivalNoticeResponseDto(
        Long id,
        Long userId,
        String content,
        LocalDateTime updatedDate,
        String title,
        List<String> images
){
    public FestivalNoticeResponseDto(FestivalNotice festivalNotice, List<String> imagesUrl){
        this(
                festivalNotice.getId(),
                festivalNotice.getAuthor().getId(),
                festivalNotice.getTitle(),
                festivalNotice.getUpdatedDate(),
                festivalNotice.getContent(),
                imagesUrl
        );
    }

}
