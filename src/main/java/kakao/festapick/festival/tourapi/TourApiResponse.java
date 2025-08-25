package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.dto.FestivalRequestDto;
import lombok.Getter;


@Getter
public class TourApiResponse {

    private List<FestivalRequestDto> festivalResponseDtoList = new ArrayList<>();

    @JsonProperty("response")
    private void unpackNested(Map<String, Object> response) {
        Map<String, Object> body = (Map<String, Object>) response.get("body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        List<Map<String, String>> itemList = (List<Map<String, String>>) items.get("item");
        festivalResponseDtoList = itemList.stream()
                .map(
                        info ->
                                new FestivalRequestDto(
                                        info.get("contentid"),
                                        info.get("title"),
                                        Integer.parseInt(info.get("areacode")),
                                        info.get("addr1"),
                                        info.get("addr2"),
                                        info.get("firstimage"),
                                        toLocalDate(info.get("eventstartdate")),
                                        toLocalDate(info.get("eventenddate"))
                                )
                )
                .toList();
    }

    private LocalDate toLocalDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

}


