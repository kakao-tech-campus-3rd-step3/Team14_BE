package kakao.festapick.fileupload.controller;

import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.fileupload.dto.PresignedUrlResponse;
import kakao.festapick.fileupload.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/presigned-url")
public class S3Controller {

    private final S3Service s3Service;

    // Presigned URL 발급받기
    @GetMapping
    public ResponseEntity<ApiResponseDto<PresignedUrlResponse>> getPresignedURL() {
        PresignedUrlResponse uploadPresignedURL = s3Service.createUploadPresignedURL();
        ApiResponseDto<PresignedUrlResponse> responseDto = new ApiResponseDto<>(uploadPresignedURL);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
