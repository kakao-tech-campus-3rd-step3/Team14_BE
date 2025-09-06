package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TourApiItems(
        @JsonProperty("item")
        List<TourApiItem> TourApiItems
) {

}
