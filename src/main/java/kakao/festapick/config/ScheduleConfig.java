package kakao.festapick.config;

import kakao.festapick.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduleConfig {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(8);
        refreshTokenRepository.deleteExpiredRefreshToken(cutoff);
    }
}