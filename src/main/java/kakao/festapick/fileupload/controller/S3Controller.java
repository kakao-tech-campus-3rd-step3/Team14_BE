package kakao.festapick.fileupload.controller;

import jakarta.validation.Valid;
import kakao.festapick.fileupload.dto.S3FileDeleteRequest;
import kakao.festapick.fileupload.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/presigned-url")
public class S3Controller {

    private final S3Service s3Service;

    // Presigned URL 발급받기
    @GetMapping
    public ResponseEntity<Map<String, String>> getPresignedURL() {
        String uploadPresignedURL = s3Service.createUploadPresignedURL();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("presignedUrl",uploadPresignedURL));
    }

    // 파일 다건 삭제 (업로드 취소할 경우 꼭 호출해줘야함)
    @PostMapping("/delete")
    public ResponseEntity<Void> deleteS3File(@Valid @RequestBody S3FileDeleteRequest s3FileDeleteRequest) {

        s3Service.deleteFiles(s3FileDeleteRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
