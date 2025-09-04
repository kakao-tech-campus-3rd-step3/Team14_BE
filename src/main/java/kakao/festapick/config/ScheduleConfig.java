package kakao.festapick.config;

import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.jwt.repository.RefreshTokenRepository;
import kakao.festapick.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduleConfig {

    private final JwtService jwtService;
    private final S3Service s3Service;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        jwtService.deleteExpiredRefreshTokens();
    }

    @Scheduled(cron = "0 10 3 * * *")
    public void removeOrphanS3File() {;
        s3Service.deleteOrphanS3Files();
    }
}