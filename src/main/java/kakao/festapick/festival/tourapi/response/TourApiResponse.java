package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiResponse(
        Body body
) {
        public record Body(
                String totalCount,
                Items items
        ) { }

        public record Items(
                List<FestivalInfo> item
        ) { }

        public record FestivalInfo(

                //기본 정보
                String contentid,
                String title,
                String areacode,
                String addr1,
                String addr2,
                String firstimage,
                String eventstartdate,
                String eventenddate,

                //detail_info
                String overview,
                String homepage

        ) { }

}
