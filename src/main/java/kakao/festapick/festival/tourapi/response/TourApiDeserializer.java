package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import kakao.festapick.festival.tourapi.response.TourApiResponse.Items;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TourApiDeserializer extends JsonDeserializer<Items> {

    private final ObjectMapper objectMapper;

    @Override
    public Items deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        if(jsonNode.has("item") && jsonNode.get("item").isArray()){
            List<FestivalInfo> festivalInfos = new ArrayList<>();
            for(JsonNode n : jsonNode.get("item")){
                festivalInfos.add(objectMapper.treeToValue(n, FestivalInfo.class));
            }
            return new Items(festivalInfos);
        }
        return new Items(List.of());
    }
}
