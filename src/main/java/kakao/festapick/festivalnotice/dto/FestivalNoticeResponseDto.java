package kakao.festapick.festivalnotice.dto;

import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.festivalnotice.domain.FestivalNotice;

public record FestivalNoticeResponseDto(
        Long id,
        Long userId,
        LocalDateTime updatedDate,
        String title,
        String content,
        List<String> images
){
    public FestivalNoticeResponseDto(FestivalNotice festivalNotice, List<String> imagesUrl){
        this(
                festivalNotice.getId(),
                festivalNotice.getAuthor().getId(),
                festivalNotice.getUpdatedDate(),
                festivalNotice.getTitle(),
                festivalNotice.getContent(),
                imagesUrl
        );
    }

}
