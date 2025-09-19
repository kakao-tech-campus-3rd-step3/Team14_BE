package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import lombok.Getter;

@Getter
public class TourImagesResponse {

    private List<FestivalInfo> imageInfos;

    private int numOfRows;

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        numOfRows = Integer.parseInt(tourApiResponse.body().numOfRows());
        imageInfos = tourApiResponse.body().items().item();
    }
}
