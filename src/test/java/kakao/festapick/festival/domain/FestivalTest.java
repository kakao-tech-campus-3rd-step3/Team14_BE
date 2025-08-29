package kakao.festapick.festival.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;

import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FestivalTest {

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
    void returnDefaultImage() {
        FestivalCustomRequestDto festivalCustomRequestDto = new FestivalCustomRequestDto("축제title", 32, "주소1", "상세주소",
                "", LocalDate.of(2025, 8, 24), LocalDate.of(2025, 8, 25), "hompage", "overivew");

        Festival festival = new Festival(festivalCustomRequestDto, new UserEntity("GOOGLE-1234",
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE));

        assertThat(festival.getImageUrl()).isNotBlank();
    }

    private Festival createFestival(){
        return new Festival();
    }

    private FestivalUpdateRequestDto createUpdateInfo(){
        return new FestivalUpdateRequestDto("update_title",
                1, "update_addr1", "update_addr2",
                "update_imageUrl", LocalDate.now(), LocalDate.now(), "hompage", "overview");
    }

}