package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import kakao.festapick.festival.tourapi.response.TourApiBody;
import kakao.festapick.festival.tourapi.response.TourApiItem;
import kakao.festapick.festival.tourapi.response.TourApiItems;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import lombok.Getter;

@Getter
public class TourDetailResponse {

    private String overview;

    private String homepage;

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        TourApiBody tourApiBody = tourApiResponse.tourApiBody();
        TourApiItems tourApiItems = tourApiBody.items();
        TourApiItem tourDetailInfo = tourApiItems.TourApiItems().getFirst();

        overview = tourDetailInfo.overview();
        homepage = tourDetailInfo.homepage();
    }

}

