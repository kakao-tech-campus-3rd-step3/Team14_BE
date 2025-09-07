package kakao.festapick.global;

import kakao.festapick.fileupload.service.S3Service;
import kakao.festapick.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final JwtService jwtService;
    private final S3Service s3Service;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        jwtService.deleteExpiredRefreshTokens();
    }

    @Scheduled(cron = "0 20 3 * * *")
    public void removeOrphanS3File() {
        s3Service.deleteOrphanS3Files();
    }
}
