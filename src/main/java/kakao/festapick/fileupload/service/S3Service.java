package kakao.festapick.fileupload.service;

import kakao.festapick.fileupload.dto.S3FileDeleteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String createUploadPresignedURL() {

        String filePath = UUID.randomUUID().toString();

        PutObjectRequest uploadRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
                r -> r.putObjectRequest(uploadRequest)
                        .signatureDuration(Duration.ofMinutes(5))
        );

        return presignedPutObjectRequest.url().toString();
    }

    public void deleteFiles(S3FileDeleteRequest s3FileDeleteRequest) {
        s3FileDeleteRequest.presignedUrls()
                .forEach(this::deleteS3File);
    }

    public void deleteS3File(String imageURL) {
        String path = extractFileName(imageURL);
        if (path.startsWith("defaultImage")) return;

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    private String extractFileName(String imageURL) {
        int pathStartIdx = imageURL.indexOf(".amazonaws.com/") + ".amazonaws.com/".length();
        int pathEndIdx = imageURL.contains("?") ? imageURL.indexOf("?") : imageURL.length();

        return imageURL.substring(pathStartIdx, pathEndIdx);
    }
}
