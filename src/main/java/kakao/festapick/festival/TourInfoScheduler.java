package kakao.festapick.festival;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalJdbcTemplateRepository;
import kakao.festapick.fileupload.repository.FileJdbcTemplateRepository;
import kakao.festapick.festival.tourapi.TourApiMaxRows;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.festival.tourapi.TourImagesResponse;
import kakao.festapick.festival.tourapi.TourInfoResponse;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
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
public class TourInfoScheduler {

    @Value("${tour.api.secret.key}")
    private String tourApiKey;

    private final RestClient restClient;

    private final FestivalJdbcTemplateRepository festivalJdbcTemplateRepository;
    private final FileJdbcTemplateRepository fileJdbcTemplateRepository;

    public TourInfoScheduler(
            RestClient.Builder builder,
            FestivalJdbcTemplateRepository festivalJdbcTemplateRepository,
            @Value("${tour.api.baseUrl}") String baseUrl,
            FileJdbcTemplateRepository fileJdbcTemplateRepository
    ) {

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

        this.festivalJdbcTemplateRepository = festivalJdbcTemplateRepository;
        this.fileJdbcTemplateRepository = fileJdbcTemplateRepository;
    }

    @GetMapping("/update") // 테스트용 - 개발 완료시 삭제할 것
    @Scheduled(cron = "0 10 3 * * *")
    public void fetchFestivals() {
        int maxRows = getMaxColumns();
        if (maxRows > 0) {
            //maxRows = 50; //for test
            log.info("가져올 축제 정보 수 : {}", maxRows);
            TourInfoResponse tourApiResponse = getFestivals(maxRows).getBody();
            List<FestivalRequestDto> festivalList = tourApiResponse.getFestivalResponseDtoList();
            List<Festival> festivals = festivalList.stream()
                    .map(requestDto -> new Festival(requestDto, getDetails(requestDto.contentId())))
                    .toList();
            festivalJdbcTemplateRepository.upsertFestivalInfo(festivals);
            log.info("축제 정보 저장 완료");
            saveImages(festivals);
        }
    }

    private void saveImages(List<Festival> festivals) {
        //새로운 이미지 저장하기
        Map<String, Long> idMap = new HashMap<>();
        List<FileEntity> files = new ArrayList<>();
        Map<String, String> posters = new HashMap<>();

        //이미지 저장을 위해서는 축제의 id가 필요함
        festivalJdbcTemplateRepository.getFestivalIds(
                        festivals.stream().map(a -> a.getContentId()).toList())
                .forEach(info -> idMap.put(info.contentId(), info.id()));

        //이미지 저장 및 대표 이미지를 포스터로 변경
        idMap.keySet()
                .stream().map(contentId -> getDetailImages(contentId))
                .filter(imagesResponse -> imagesResponse.getNumOfRows() > 0)
                .forEach(imagesResponse -> imagesResponse.getImageInfos()
                        .forEach(imageInfo -> {
                            if(imageInfo.imgname().contains("포스터")){
                                posters.put(imageInfo.contentid(), imageInfo.originimgurl());
                            }
                            else{
                                files.add(new FileEntity(imageInfo.originimgurl(), FileType.IMAGE, DomainType.FESTIVAL, idMap.get(imageInfo.contentid())));
                            }
                        }
                ));

        fileJdbcTemplateRepository.insertFestivalImages(files); // 축제 관련 이미지 저장하기
        festivalJdbcTemplateRepository.updatePosters(posters); // 대표 이미지를 poster로 upsert
        log.info("축제 관련 사진 저장완료");
    }


    private ResponseEntity<TourInfoResponse> getFestivals(int numOfRows) {
        ResponseEntity<TourInfoResponse> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .retrieve()
                .toEntity(TourInfoResponse.class);
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

    private TourImagesResponse getDetailImages(String contentId) {
        ResponseEntity<TourImagesResponse> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/detailImage2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("contentId", contentId)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .toEntity(TourImagesResponse.class);
        return response.getBody();
    }


    private int getMaxColumns() {
        ResponseEntity<TourApiMaxRows> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 1)
                        .build())
                .retrieve()
                .toEntity(TourApiMaxRows.class);
        return (response.getBody() != null) ? response.getBody().getMaxColumns() : 0;
    }

    private String getDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(date);
    }

}
