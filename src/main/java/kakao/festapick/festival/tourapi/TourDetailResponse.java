package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TourDetailResponse {

    private String overview;

    private String homepage;

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        FestivalInfo tourDetailInfo = tourApiResponse.body().items().item().getFirst();
        overview = tourDetailInfo.overview();
        homepage = parseUrl(tourDetailInfo.homepage());
    }

    private String parseUrl(String homePage){
        try{
            List<String> parsedResult = Arrays.asList(homePage.split("\""));
            return parsedResult.stream()
                    .filter(url -> url.startsWith("http") || url.startsWith("www."))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("홈페이지의 주소를 찾을 수 없습니다."));
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("홈페이지 정보를 찾을 수 없습니다.");
            log.error("homePage = {}", homePage);
        }
        return "no_homepage";
    }

}

