package kakao.festapick.festival.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TourDetailResponseTest {

    @Test
    @DisplayName("홈페이지 정보가 없는 경우")
    void getHomePageParsingFail(){

        //given
        TourDetailResponse tourDetailInfo = new TourDetailResponse();
        String homepage = null;

        //when
        String result = ReflectionTestUtils.invokeMethod(tourDetailInfo, "parseUrl", homepage);

        //then
        assertThat(result).isEqualTo("no_homepage");
    }

    @Test
    @DisplayName("기존의 형태(패턴)와 다르게 homepage 정보가 제공되는 경우 파싱에 실패")
    void getHomePagePatternError(){

        //given
        TourDetailResponse tourDetailInfo = new TourDetailResponse();
        String homepage = "<a href\"html://www.festapick.com\">www.festapick.com</a>";

        //when
        String result = ReflectionTestUtils.invokeMethod(tourDetailInfo, "parseUrl", homepage);

        //then
        assertThat(result).isEqualTo("no_homepage");
    }

    @Test
    @DisplayName("홈페이지 정보 파싱 테스트")
    void getHomePage(){

        //given
        TourDetailResponse tourDetailInfo = new TourDetailResponse();
        String homepage = "<a href\"https://www.festapick.com\">www.festapick.com</a>";

        //when
        String result = ReflectionTestUtils.invokeMethod(tourDetailInfo, "parseUrl", homepage);

        //then
        assertThat(result).isEqualTo("https://www.festapick.com");
    }



}