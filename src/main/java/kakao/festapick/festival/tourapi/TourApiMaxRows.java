package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;

@Getter
public class TourApiMaxRows {

    private int maxColumns;

    @JsonProperty("response")
    private void unpackNested(Map<String, Object> response) {
        Map<String, Object> body = (Map<String, Object>) response.get("body");
        String totalCount = body.get("totalCount").toString();
        maxColumns = Integer.parseInt(totalCount);
    }

}
