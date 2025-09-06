package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import lombok.Getter;


@Getter
public class TourInfoResponse {

    private List<FestivalRequestDto> festivalResponseDtoList = new ArrayList<>();

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        List<FestivalInfo> tourApiItemList = tourApiResponse.body().items().item();

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


