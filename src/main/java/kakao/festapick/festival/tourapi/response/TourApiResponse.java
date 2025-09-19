package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiResponse(
        Body body
) {
        public record Body(
                String numOfRows,
                String totalCount,
                Items items
        ) { }

        @JsonDeserialize(using = TourApiDeserializer.class)
        public record Items(
                List<FestivalInfo> item
        ) {
        }

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
                String homepage,

                //detail_img
                String originimgurl,
                String imgname

        ) { }

}
