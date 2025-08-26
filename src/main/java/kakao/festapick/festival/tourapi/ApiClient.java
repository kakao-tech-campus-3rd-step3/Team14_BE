package kakao.festapick.festival.tourapi;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.service.FestivalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

@Slf4j
@RestController
@RequestMapping("/api/festivals")
public class ApiClient {

    @Value("${tour.api.secret.key}")
    private String tourApiKey;

    private String baseUrl = "https://apis.data.go.kr";

    private final RestClient restClient;

    private final FestivalService festivalService;

    public ApiClient(RestClient.Builder builder, FestivalService festivalService) {

        //TODO: make Config for RestClient
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(EncodingMode.NONE);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = builder
                .requestFactory(requestFactory)
                .uriBuilderFactory(uriBuilderFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        (req, res) -> log.error("restclient에서 발생한 400 오류(클라이언트 에러)")
                )
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> log.error("restClient에서 발생한 500 오류(서버 에러)")
                )
                .build();
        this.festivalService = festivalService;
    }

    @GetMapping("/update")
    @Scheduled(cron = "0 10 3 * * *")
    public void fetchFestivals() {
        TourApiResponse tourApiResponse = getFestivals(getMaxColumns()).getBody();
        List<FestivalRequestDto> festivalList = tourApiResponse.getFestivalResponseDtoList();
        festivalList.stream()
                .filter(requestDto -> festivalService.checkExistenceByContentId(requestDto.contentId()))
                .forEach(requestDto -> festivalService.addFestival(requestDto, getDetails(requestDto.contentId())));
    }

    private ResponseEntity<TourApiResponse> getFestivals(int numOfRows) {
        ResponseEntity<TourApiResponse> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .retrieve()
                .toEntity(TourApiResponse.class);
        return response;
    }

    private TourDetailResponse getDetails(String contentId) {
        ResponseEntity<TourDetailResponse> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/detailCommon2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("contentId", contentId)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .toEntity(TourDetailResponse.class);
        return response.getBody();
    }

    private int getMaxColumns(){
        ResponseEntity<TourApiMaxColumns> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 1)
                        .build())
                .retrieve()
                .toEntity(TourApiMaxColumns.class);
        return (response.getBody() != null) ? response.getBody().getMaxColumns() : 0;
    }


    private String getDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(date);
    }

}
