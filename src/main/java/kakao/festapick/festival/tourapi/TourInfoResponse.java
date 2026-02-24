package kakao.festapick.festival.tourapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.response.TourApiResponse;
import kakao.festapick.festival.tourapi.response.TourApiResponse.FestivalInfo;
import lombok.Getter;


@Getter
public class TourInfoResponse {

    private List<FestivalRequestDto> festivalResponseDtoList = new ArrayList<>();

    private static final HashMap<String, Integer> areaMap = new HashMap<>();

    static {

        areaMap.put("서울", 1);
        areaMap.put("서울특별시", 1);

        areaMap.put("인천", 2);
        areaMap.put("인천광역시", 2);

        areaMap.put("대전", 3);
        areaMap.put("대전광역시", 3);

        areaMap.put("대구", 4);
        areaMap.put("대구광역시", 4);

        areaMap.put("광주", 5);
        areaMap.put("광주광역시", 5);

        areaMap.put("부산", 6);
        areaMap.put("부산광역시", 6);

        areaMap.put("울산", 7);
        areaMap.put("울산광역시", 7);

        areaMap.put("경기", 31);
        areaMap.put("경기도", 31);

        areaMap.put("강원", 32);
        areaMap.put("강원도", 32);
        areaMap.put("강원특별자치도", 32);

        areaMap.put("충북", 33);
        areaMap.put("충청북도", 33);

        areaMap.put("충남", 34);
        areaMap.put("충청남도", 34);

        areaMap.put("경북", 35);
        areaMap.put("경상북도", 35);

        areaMap.put("경남", 36);
        areaMap.put("경상남도", 36);

        areaMap.put("전라특별자치도", 37);
        areaMap.put("전라북도", 37);
        areaMap.put("전북", 37);

        areaMap.put("전라남도", 38);
        areaMap.put("전남", 38);

        areaMap.put("제주특별자치도", 39);
        areaMap.put("제주도", 39);

    }

    @JsonProperty("response")
    private void unpackNested(TourApiResponse tourApiResponse) {
        List<FestivalInfo> tourApiItemList = tourApiResponse.body().items().item();

        festivalResponseDtoList = tourApiItemList.stream()
                .map(
                        info ->
                                new FestivalRequestDto(
                                        info.contentid(),
                                        info.title(),
                                        getAreaCode(info.areacode(), info.addr1()),
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

    private int getAreaCode(String areaCode, String addr1){
        if(areaCode != null && !areaCode.isBlank()) return Integer.parseInt(areaCode);
        if(addr1 != null && !addr1.isBlank()){
            String area = addr1.trim().split(" ")[0];
            return areaMap.getOrDefault(area, 0);
        }
        return 0;
    }

}


