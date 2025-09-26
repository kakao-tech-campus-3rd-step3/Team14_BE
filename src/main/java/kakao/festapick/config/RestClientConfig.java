package kakao.festapick.config;

import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import java.time.Duration;

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
                        (req, res) -> log.error("4xxClientError"))
                .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                        (req, res) -> log.error("is5xxServerError")).build();

    }

    @Bean
    public RestClient fastApiClient(
            RestClient.Builder builder,
            @Value("${fastapi.server.domain}") String baseUrl
    ) {

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(EncodingMode.NONE);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(60));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        return builder.requestFactory(requestFactory).uriBuilderFactory(uriBuilderFactory)
                .defaultStatusHandler(HttpStatusCode::isError,
                        (req, res) -> {
                            log.error("error code {}", res.getStatusCode());
                            log.error("error message {}", res.getBody());
                            throw new ExternalApiException(ExceptionCode.FAST_API_CONNECTION_ERROR);
                        }).build();
    }

}
