package kakao.festapick.festival;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalJdbcTemplateRepository;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.tourapi.TourApiMaxRows;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.festival.tourapi.TourImagesResponse;
import kakao.festapick.festival.tourapi.TourInfoResponse;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.repository.FileJdbcTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festivals") // 나중에 제거하기
public class TourInfoScheduler {

    @Value("${tour.api.secret.key}")
    private String tourApiKey;

    private final FestivalJdbcTemplateRepository festivalJdbcTemplateRepository;

    private final FestivalRepository festivalRepository;

    private final FileJdbcTemplateRepository fileJdbcTemplateRepository;

    private final RestClient tourApiClient;

    @GetMapping("/update") //// 테스트용 - 개발 완료시 삭제할 것
    @Transactional
    @Scheduled(cron = "0 15 17 * * *")
    public void fetchFestivals() {
        int maxRows = getMaxColumns();
        if (maxRows > 0) {
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
        festivalRepository.findFestivalsByContentIds(
                festivals.stream().map(festival -> festival.getContentId()).toList())
                .forEach(festival -> idMap.put(festival.getContentId(), festival.getId()));

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
        ResponseEntity<TourInfoResponse> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(TourInfoResponse.class);
        return response;
    }

    private TourDetailResponse getDetails(String contentId) {
        ResponseEntity<TourDetailResponse> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/detailCommon2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("contentId", contentId)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(TourDetailResponse.class);
        return response.getBody();
    }

    private TourImagesResponse getDetailImages(String contentId) {
        ResponseEntity<TourImagesResponse> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/detailImage2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("contentId", contentId)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(TourImagesResponse.class);
        return response.getBody();
    }

    private int getMaxColumns() {
        ResponseEntity<TourApiMaxRows> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 1)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
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
