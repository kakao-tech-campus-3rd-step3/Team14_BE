package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import kakao.festapick.festival.tourapi.response.TourApiBody;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import lombok.Getter;

@Getter
public class TourApiMaxRows {

    private int maxColumns;

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        TourApiBody tourApiBody = tourApiResponse.tourApiBody();
        String totalCount = tourApiBody.totalCount();
        maxColumns = Integer.parseInt(totalCount);
    }

}