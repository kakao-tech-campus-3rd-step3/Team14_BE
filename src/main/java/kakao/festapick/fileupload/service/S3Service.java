package kakao.festapick.fileupload.service;

import kakao.festapick.fileupload.domain.TemporalFile;
import kakao.festapick.fileupload.dto.PresignedUrlResponse;
import kakao.festapick.fileupload.repository.TemporalFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final TemporalFileRepository temporalFileRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public PresignedUrlResponse createUploadPresignedURL() {

        String filePath = UUID.randomUUID().toString();

        PutObjectRequest uploadRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
                r -> r.putObjectRequest(uploadRequest)
                        .signatureDuration(Duration.ofMinutes(5))
        );

        String fileUrl = presignedPutObjectRequest.url().toString();

        TemporalFile saved = temporalFileRepository.save(new TemporalFile(extractFileUrl(fileUrl)));

        return new PresignedUrlResponse(saved.getId(), fileUrl);
    }

    public void deleteOrphanS3Files() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        List<TemporalFile> orphanFiles = temporalFileRepository.findOrphanFiles(cutoff);
        List<String> urls = orphanFiles.stream()
                        .map(TemporalFile::getUrl).toList();

        deleteFiles(urls);

        List<Long> temporalFileIds = orphanFiles.stream().map(TemporalFile::getId).toList();

        temporalFileRepository.deleteByIds(temporalFileIds);
    }

    public void deleteFiles(List<String> urls) {
        urls.forEach(this::deleteS3File);
    }

    public void deleteS3File(String fileUrl) {
        if(fileUrl.startsWith("http://tong.visitkorea")) return;

        String path = extractFileName(fileUrl);
        if (path.startsWith("defaultImage")) return;

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    private String extractFileName(String fileUrl) {
        int pathStartIdx = fileUrl.contains(".amazonaws.com/") ?
        fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length() : 0;

        int pathEndIdx = fileUrl.contains("?") ? fileUrl.indexOf("?") : fileUrl.length();

        return fileUrl.substring(pathStartIdx, pathEndIdx);
    }

    private String extractFileUrl(String fileUrl) {
        int parameterIdx = fileUrl.indexOf("?");

        return fileUrl.substring(0, parameterIdx);
    }
}
