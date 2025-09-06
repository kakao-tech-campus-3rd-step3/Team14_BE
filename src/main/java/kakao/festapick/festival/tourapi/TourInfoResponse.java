package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.response.TourApiBody;
import kakao.festapick.festival.tourapi.response.TourApiItem;
import kakao.festapick.festival.tourapi.response.TourApiItems;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import lombok.Getter;


@Getter
public class TourInfoResponse {

    private List<FestivalRequestDto> festivalResponseDtoList = new ArrayList<>();

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        TourApiBody tourApiBody = tourApiResponse.tourApiBody();
        TourApiItems items = tourApiBody.items();
        List<TourApiItem> tourApiItemList = items.TourApiItems();

        festivalResponseDtoList = tourApiItemList.stream()
                .map(
                        info ->
                                new FestivalRequestDto(
                                        info.contentid(),
                                        info.title(),
                                        Integer.parseInt(info.areacode()),
                                        info.addr1(),
                                        info.addr2(),
                                        info.firstimage(),
                                        toLocalDate(info.eventstartdate()),
                                        toLocalDate(info.eventenddate())
                                )
                )
                .toList();
    }

    private LocalDate toLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

}


