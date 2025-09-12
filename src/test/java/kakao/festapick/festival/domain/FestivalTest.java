package kakao.festapick.festival.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import kakao.festapick.config.DefaultImageConfig;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.global.DefaultImageProperties;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FestivalTest {

    private final TestUtil testUtil = new TestUtil();

    @BeforeEach
    void setUp() {
        DefaultImageProperties props = new DefaultImageProperties();
        props.setFestival("festival_default.png");
        props.setProfile("profile_default.png");
        new DefaultImageConfig(props).init();
    }

    @Test
    void updateFestival() {

        //given
        Festival festival = createFestival();
        FestivalUpdateRequestDto updateInfo = createUpdateInfo();

        //when
        festival.updateFestival(updateInfo);

        //then
        assertAll(
                () -> assertThat(festival.getTitle()).isEqualTo(updateInfo.title()),
                () -> assertThat(festival.getAddr1()).isEqualTo(updateInfo.addr1())
        );
    }

    @Test
    void updateState() {

        //given
        Festival festival = createFestival();
        FestivalState festivalState = FestivalState.APPROVED;

        //when
        festival.updateState(festivalState);

        //then
        assertThat(festival.getState()).isEqualTo(festivalState);
    }

    @Test
    @DisplayName("이미지 url이 빈값일때, 기본 이미지 반환")
    void returnDefaultImage() throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("contentId", "축제title", 32, "주소1", "상세주소", null, testUtil.toLocalDate("20250804"),testUtil.toLocalDate("20250806"));
        Festival festival = new Festival(festivalRequestDto, testUtil.createTourDetailResponse());
        assertThat(festival.getPosterInfo()).isNotBlank();
    }

    @Test
    @DisplayName("홈페이지 정보가 없는 경우")
    void getHomePageParsingFail(){

        //given
        Festival festival = new Festival();
        String homepage = null;

        //when
        String result = ReflectionTestUtils.invokeMethod(festival, "parseHomepageInfo", homepage);

        //then
        assertThat(result).isEqualTo("no_homepage");
    }

    @Test
    @DisplayName("기존의 형태(패턴)와 다르게 homepage 정보가 제공되는 경우 파싱에 실패")
    void getHomePagePatternError(){

        //given
        Festival festival = new Festival();
        String homepage = "<a href\"html://www.festapick.com\">www.festapick.com</a>";

        //when
        String result = ReflectionTestUtils.invokeMethod(festival, "parseHomepageInfo", homepage);

        //then
        assertThat(result).isEqualTo("no_homepage");
    }

    private Festival createFestival(){
        return new Festival();
    }

    private FestivalUpdateRequestDto createUpdateInfo(){
        return new FestivalUpdateRequestDto("update_title",
                1, "update_addr1", "update_addr2",
                new FileUploadRequest(1L,"update_imageUrl"), null, LocalDate.now(), LocalDate.now(), "hompage", "overview");
    }

}