package kakao.festapick.festival.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FestivalRepositoryTest {

    @Autowired private FestivalRepository festivalRepository;

    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setFestivalRepository(){
        String identifier = "GOOGLE-1234";
        UserEntity user = createUser(identifier);
        userRepository.save(user);

        Festival festival1 = createFestival("FESTAPICK_001" , "부산대축제", 1, toLocalDate("20250810"), toLocalDate("20250820"));
        festivalRepository.save(festival1);

        Festival festival2 = createFestival("FESTAPICK_002", "경북대축제", 2, toLocalDate("20250812"), toLocalDate("20250815"));
        festivalRepository.save(festival2);

        Festival festival3 = createFestival("FESTAPICK_003","정컴인축제",  1, toLocalDate("20250814"), toLocalDate("20250817"));
        festivalRepository.save(festival3);

        Festival festival4 = creatCustomFestival("의생공축제",  2, toLocalDate("20250817"), toLocalDate("20250818"), user);
        festivalRepository.save(festival4);

        Festival festival5 = creatCustomFestival("밀양대축제", 3, toLocalDate("20250821"), toLocalDate("20250823"), user);
        festivalRepository.save(festival5);
    }

    @Test
    @DisplayName("ContentId를 통해 축제를 가져오기")
    void findFestivalByContentId() {

        //given
        String contentId = "FESTAPICK_999";
        Festival festival = createFestival(contentId , "카테캠축제", 1, toLocalDate("20250817"), toLocalDate("20250821"));
        festivalRepository.save(festival);

        //when
        Optional<Festival> foundOne = festivalRepository.findFestivalByContentId(contentId);

        //then
        assertThat(foundOne).isNotEmpty();
        Festival actual = foundOne.get();
        assertAll(
                () -> assertThat(actual.getContentId()).isEqualTo(contentId),
                () -> assertThat(actual.getTitle()).isEqualTo("카테캠축제"),
                () -> assertThat(actual.getState()).isEqualTo(FestivalState.APPROVED)
        );
    }

    @Test
    @DisplayName("Approved 축제 모두 조회")
    void findAllByStateApproved() {

        //when
        List<Festival> festivals = festivalRepository.findAllByState(FestivalState.APPROVED);

        //then
        assertAll(
                () -> assertThat(festivals.size()).isEqualTo(3),
                () -> assertThat(festivals.getFirst().getState()).isEqualTo(FestivalState.APPROVED)
        );
    }

    @Test
    @DisplayName("Processing 축제 모두 조회")
    void findAllByStateProcessing() {

        //when
        List<Festival> festivals = festivalRepository.findAllByState(FestivalState.PROCESSING);

        //then
        assertAll(
                () -> assertThat(festivals.size()).isEqualTo(2),
                () -> assertThat(festivals.getFirst().getState()).isEqualTo(FestivalState.PROCESSING)
        );
    }

    @Test
    void findFestivalByAreaCodeAndDate() {

        //given
        int areaCode = 1;

        Festival festival1 = createFestival("FESTAPICK_111" , "카테캠축제", areaCode, toLocalDate("20250815"), toLocalDate("20250820"));
        festivalRepository.save(festival1);

        Festival festival2 = createFestival("FESTAPICK_222" , "스파르타축제", areaCode, toLocalDate("20250810"), toLocalDate("20250814"));
        festivalRepository.save(festival2);

        //when
        List<Festival> festivals = festivalRepository.findFestivalByAreaCodeAndDate(areaCode, toLocalDate("20250816"), FestivalState.APPROVED);

        //then
        assertAll(
                () -> assertThat(festivals.size()).isGreaterThan(1),
                () -> assertThat(festivals).contains(festival1),
                () -> assertThat(festivals).doesNotContain(festival2),
                () -> assertThat(festivals.getFirst().getAreaCode()).isEqualTo(areaCode)
        );
    }

    @Test
    void findFestivalById() {
        //given
        Festival festival = createFestival("축제1", "주소1", 1, toLocalDate("20250815"), toLocalDate("20250820"));
        Festival savedFestival = festivalRepository.save(festival);

        //when
        Optional<Festival> foundOne = festivalRepository.findFestivalById(savedFestival.getId());

        //then
        assertThat(foundOne).isNotEmpty();
        Festival actual = foundOne.get();

        assertAll(
                () -> assertThat(actual.getTitle()).isEqualTo(festival.getTitle()),
                () -> assertThat(actual.getOverView()).isEqualTo(festival.getOverView())
        );
    }

    @Test
    void findFestivalByIdWithManager() {

        //given
        String identifier = "KAKAO-141036";
        UserEntity user = createUser(identifier);
        userRepository.save(user);
        Festival festival = creatCustomFestival("축제1", 2, toLocalDate("20250815"), toLocalDate("20250820"), user);
        festivalRepository.save(festival);

        //when
        Optional<Festival> foundOne = festivalRepository.findFestivalByIdWithManager(festival.getId());

        //then
        assertThat(foundOne).isNotEmpty();
        Festival actual = foundOne.get();

        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(festival.getId()),
                () -> assertThat(actual.getTitle()).isEqualTo(festival.getTitle()),
                () -> assertThat(actual.getManager()).isEqualTo(user)
        );
    }

    @Test
    void findFestivalByTitleContainingAndState() {
    }

    @Test
    void findFestivalByAreaCodeAndState() {
    }

    private FestivalRequestDto FestivalRequest(String contentId, String title, int areaCode, LocalDate startDate, LocalDate endDate){
        return new FestivalRequestDto(
                contentId, title, areaCode, "addr1", "addr2",
                "imageUrl", startDate, endDate
        );
    }

    private Festival createFestival(String contentId, String title, int areaCode, LocalDate startDate, LocalDate endDate){
        return new Festival(FestivalRequest(contentId, title, areaCode, startDate, endDate), "overview", "homePage");
    }

    private CustomFestivalRequestDto customFestivalRequest(String title, int areaCode, LocalDate startDate, LocalDate endDate){
        return new CustomFestivalRequestDto(
                 title, areaCode, "addr1", "addr2",
                "imageUrl", startDate, endDate, "homePage", "overView"
        );
    }

    private Festival creatCustomFestival(String title, int areaCode, LocalDate startDate, LocalDate endDate, UserEntity user){
        return new Festival(customFestivalRequest(title, areaCode, startDate, endDate), user);
    }

    private UserEntity createUser(String identifier){
        return new UserEntity(identifier, "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE);
    }

    private LocalDate toLocalDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

}