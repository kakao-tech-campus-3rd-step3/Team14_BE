package kakao.festapick.festival.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import kakao.festapick.festival.dto.FestivalRequestDto;
import org.junit.jupiter.api.Test;

class FestivalTest {

    @Test
    void updateFestival() {

        //given
        Festival festival = createFestival();
        FestivalRequestDto updateInfo = createUpdateInfo();

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

    private Festival createFestival(){
        return new Festival();
    }

    private FestivalRequestDto createUpdateInfo(){
        return new FestivalRequestDto(    "contentId", "update_title",
                1, "update_addr1", "update_addr2",
                "update_imageUrl", LocalDate.now(), LocalDate.now());
    }

}