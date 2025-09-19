package kakao.festapick.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

@Slf4j
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient tourApiClient(
            RestClient.Builder builder,
            @Value("${tour.api.baseurl}") String baseUrl
    ) {

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(EncodingMode.NONE);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(120));
        requestFactory.setReadTimeout(Duration.ofSeconds(120));

        return builder.requestFactory(requestFactory).uriBuilderFactory(uriBuilderFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        (req, res) -> log.error("restClient에서 발생한 400 오류(클라이언트 에러)"))
                .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                        (req, res) -> log.error("restClient에서 발생한 500 오류(서버 에러)")).build();

    }

}
