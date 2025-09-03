package kakao.festapick.fileupload.dto;

import kakao.festapick.fileupload.domain.TemporalFile;

public record PresignedUrlResponse(
        Long id,
        String presignedUrl
) {

    public PresignedUrlResponse(TemporalFile temporalFile) {
        this(temporalFile.getId(), temporalFile.getUrl());
    }
}
