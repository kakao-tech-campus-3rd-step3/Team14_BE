package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record TourApiItem(

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

) {

}
