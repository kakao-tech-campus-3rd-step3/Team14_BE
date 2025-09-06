package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import lombok.Getter;

@Getter
public class TourDetailResponse {

    private String overview;

    private String homepage;

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        FestivalInfo tourDetailInfo = tourApiResponse.body().items().item().getFirst();
        overview = tourDetailInfo.overview();
        homepage = tourDetailInfo.homepage();
    }

}

