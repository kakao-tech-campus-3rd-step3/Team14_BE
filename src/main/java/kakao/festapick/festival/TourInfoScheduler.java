package kakao.festapick.festival;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalJdbcTemplateRepository;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festival.tourapi.TourApiMaxRows;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.festival.tourapi.TourImagesResponse;
import kakao.festapick.festival.tourapi.TourInfoResponse;
import kakao.festapick.festival.tourapi.response.TourApiExceptionMessage;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.repository.FileJdbcTemplateRepository;
import kakao.festapick.global.exception.JsonParsingException;
import kakao.festapick.global.exception.TourApiException;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festivals") // 나중에 제거하기
public class TourInfoScheduler {

    @Value("${tour.api.secret.key}")
    private String tourApiKey;

    private final FestivalJdbcTemplateRepository festivalJdbcTemplateRepository;

    private final FestivalLowService festivalLowService;

    private final FileJdbcTemplateRepository fileJdbcTemplateRepository;

    private final RestClient tourApiClient;

    private final ObjectMapper objectMapper;

    @GetMapping("/update") //// 테스트용 - 개발 완료시 삭제할 것
    @Transactional
    @Scheduled(cron = "0 10 3 * * *")
    public void fetchFestivals(){
        int maxRows = getMaxColumns();
        if (maxRows > 0) {
            log.info("가져올 축제 정보 수 : {}", maxRows);
            TourInfoResponse tourApiResponse = getFestivals(maxRows);
            List<FestivalRequestDto> festivalList = tourApiResponse.getFestivalResponseDtoList();

            List<Festival> festivals = new ArrayList<>();
            for (FestivalRequestDto requestDto : festivalList) {
                festivals.add(new Festival(requestDto, getDetails(requestDto.contentId())));
            }

            festivalJdbcTemplateRepository.upsertFestivalInfo(festivals);
            log.info("축제 정보 저장 완료");

            //이미지 저장을 위해서는 축제의 id가 필요함
            Map<String, Long> idMap = new HashMap<>();
            List<String> contentIds = festivals.stream().map(festival -> festival.getContentId()).toList();
            festivalLowService.findFestivalsByContentIds(contentIds)
                    .forEach(festival -> idMap.put(festival.getContentId(), festival.getId()));

            saveImages(idMap);
        }
    }

    private void saveImages(Map<String, Long> idMap){
        //새로운 이미지 저장하기
        List<FileEntity> files = new ArrayList<>();
        Map<String, String> posters = new HashMap<>();

        //이미지 저장 및 대표 이미지를 포스터로 변경
        List<TourImagesResponse> tourImagesResponseList = new ArrayList<>();
        for (String contentId : idMap.keySet()) {
            TourImagesResponse imagesResponse = getDetailImages(contentId);
            if(imagesResponse.getNumOfRows() > 0){
                tourImagesResponseList.add(imagesResponse);
            }
        }

        for (TourImagesResponse tourImagesResponse : tourImagesResponseList) {
            for (FestivalInfo imageInfo : tourImagesResponse.getImageInfos()) {
                if(imageInfo.imgname().contains("포스터")){
                    posters.put(imageInfo.contentid(), imageInfo.originimgurl());
                }
                else{
                    files.add(new FileEntity(imageInfo.originimgurl(), FileType.IMAGE, DomainType.FESTIVAL, idMap.get(imageInfo.contentid())));
                }
            }
        }

        fileJdbcTemplateRepository.insertFestivalImages(files); // 축제 관련 이미지 저장하기
        festivalJdbcTemplateRepository.updatePosters(posters); // 대표 이미지를 poster로 upsert
        log.info("축제 관련 사진 저장완료");
    }


    private TourInfoResponse getFestivals(int numOfRows){
        ResponseEntity<String> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .toEntity(String.class);

        String body = response.getBody();
        MediaType mediaType = response.getHeaders().getContentType();

        // 정상적인 응답이 들어온 경우
        if(mediaType != null && mediaType.includes(MediaType.APPLICATION_JSON)){
            try {
                return objectMapper.readValue(body, TourInfoResponse.class);
            } catch (JsonProcessingException e) {
                throw new JsonParsingException("전체 축제 조회에 대한 Json 파싱 실패");
            }
        }

        throw new TourApiException(getErrorMessage(body));
    }

    private TourDetailResponse getDetails(String contentId){
        ResponseEntity<String> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/detailCommon2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("contentId", contentId)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .toEntity(String.class);

        String body = response.getBody();
        MediaType mediaType = response.getHeaders().getContentType();

        if(mediaType != null && mediaType.includes(MediaType.APPLICATION_JSON)){
            try {
                return objectMapper.readValue(body, TourDetailResponse.class);
            } catch (JsonProcessingException e) {
                throw new JsonParsingException("축제 상세 정보(개요, 홈페이지) 조회에 대한 Json 파싱 실패");
            }
        }

        throw new TourApiException(getErrorMessage(body));
    }

    private TourImagesResponse getDetailImages(String contentId){
        ResponseEntity<String> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/detailImage2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("contentId", contentId)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .toEntity(String.class);

        String body = response.getBody();
        MediaType mediaType = response.getHeaders().getContentType();

        if(mediaType != null && mediaType.includes(MediaType.APPLICATION_JSON)){
            try {
                return objectMapper.readValue(body, TourImagesResponse.class);
            } catch (JsonProcessingException e) {
                throw new JsonParsingException("축제 상세 이미지 조회에 대한 Json 파싱 실패");
            }
        }

        throw new TourApiException(getErrorMessage(body));
    }

    private int getMaxColumns(){
        ResponseEntity<String> response = tourApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/B551011/KorService2/searchFestival2")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "FestaPick")
                        .queryParam("eventStartDate", getDate())
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 1)
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .toEntity(String.class);

        String body = response.getBody();
        MediaType mediaType = response.getHeaders().getContentType();

        if(mediaType != null && mediaType.includes(MediaType.APPLICATION_JSON)){
            try {
                TourApiMaxRows tourApiMaxRows = objectMapper.readValue(body, TourApiMaxRows.class);
                return tourApiMaxRows.getMaxColumns();
            } catch (JsonProcessingException e) {
                throw new JsonParsingException("전체 축제 수 조회에 대한 Json 파싱 실패");
            }
        }

        throw new TourApiException(getErrorMessage(body));
    }

    // TourAPI로 부터 오류 응답(XML)이 들어오는 경우
    private String getErrorMessage(String xml){
        XmlMapper xmlMapper = new XmlMapper();
        TourApiExceptionMessage tourApiExceptionMessage = null;
        try {
            tourApiExceptionMessage = xmlMapper.readValue(xml, TourApiExceptionMessage.class);
            return tourApiExceptionMessage.getCmmMsgHeader().getReturnAuthMsg();
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("TourAPI 오류 응답(XML) 파싱 에러");
        }
    }

    private String getDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(date);
    }

}
