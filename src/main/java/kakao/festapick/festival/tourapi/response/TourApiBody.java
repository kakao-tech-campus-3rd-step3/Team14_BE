package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record TourApiBody(
        String totalCount,
        TourApiItems items
) {

}
