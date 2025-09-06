package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TourApiResponse(
        @JsonProperty("body")
        TourApiBody tourApiBody
) {

}
