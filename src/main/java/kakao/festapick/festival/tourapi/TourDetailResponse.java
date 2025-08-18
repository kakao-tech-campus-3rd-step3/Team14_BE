package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class TourDetailResponse {

    private String overview;

    private String homepage;

    @JsonProperty("response")
    private void unpackNested(Map<String, Object> response) {
        Map<String, Object> body = (Map<String, Object>) response.get("body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        List<Map<String, String>> item = (List<Map<String, String>>) items.get("item");
        overview = item.getFirst().get("overview");
        homepage = item.getFirst().get("homepage");
    }

}

