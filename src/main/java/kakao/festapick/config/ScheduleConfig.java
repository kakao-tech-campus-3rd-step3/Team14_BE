package kakao.festapick.config;

import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduleConfig {

    private final RefreshTokenRepository refreshTokenRepository;
    private final S3Service s3Service;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(8);
        refreshTokenRepository.deleteExpiredRefreshToken(cutoff);
    }

    @Scheduled(cron = "0 28 21 * * *")
    public void removeOrphanS3File() {;
        s3Service.deleteOrphanS3Files();
    }
}