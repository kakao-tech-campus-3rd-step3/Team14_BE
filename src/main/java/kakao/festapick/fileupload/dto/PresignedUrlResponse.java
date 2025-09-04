package kakao.festapick.fileupload.dto;


public record PresignedUrlResponse(
        Long id,
        String presignedUrl
) {
}
