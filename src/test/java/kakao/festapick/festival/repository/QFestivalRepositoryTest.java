package kakao.festapick.festival.repository;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import java.util.stream.Collectors;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class QFestivalRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    private QFestivalRepository qFestivalRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private UserRepository userRepository;

    private TestUtil testUtil = new TestUtil();

    @BeforeEach
    void init() {
        qFestivalRepository = new QFestivalRepository(entityManager);
    }

    @Test
    @DisplayName("지역 코드를 통해 축제를 조회하는 경우")
    void findFestivalByAreaCodeAndDate() throws Exception {
        //given
        int areaCode = 1;

        Festival festival1 = createFestivalAndSaveByAreaCode(areaCode);
        createFestivalAndSaveByAreaCode(areaCode);
        createFestivalAndSaveByAreaCode(3);

        //when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Festival> festivals = qFestivalRepository.findFestivalByAreaCodeAndDate(areaCode,
                testUtil.toLocalDate("20250816"), pageable);

        //then
        assertSoftly(
                softly -> {
                    softly.assertThat(festivals.getTotalElements()).isEqualTo(2);
                    softly.assertThat(festivals).contains(festival1);
                    softly.assertThat(festivals.getContent().getFirst().getAreaCode())
                            .isEqualTo(areaCode);
                });
    }

    @Test
    @DisplayName("전국 축제를 조회하는 경우")
    void findAllFestivalAndDate() throws Exception {
        //given
        Integer areaCode = null;

        Festival festival1 = createFestivalAndSaveByAreaCode(1);
        Festival festival2 = createFestivalAndSaveByAreaCode(2);
        createFestivalAndSaveByAreaCode(3);
        createFestivalAndSaveByAreaCode(4);

        //when
        Pageable pageable = PageRequest.of(0, 5);
        Page<Festival> festivals = qFestivalRepository.findFestivalByAreaCodeAndDate(areaCode,
                testUtil.toLocalDate("20250816"), pageable);

        //then
        assertSoftly(
                softly -> {
                    softly.assertThat(festivals.getTotalElements()).isEqualTo(4);
                    softly.assertThat(festivals).contains(festival1);
                    softly.assertThat(festivals).contains(festival2);
                    softly.assertThat(festivals.getContent().stream().map(Festival::getAreaCode)
                            .collect(Collectors.toSet()).size()).isEqualTo(4);
                });
    }

    @Test
    @DisplayName("custom 축제이면서 상태가 approved인 축제 조회하기")
    void findByStateAndTitleLikeAndType() throws Exception {
        //given
        UserEntity manager = testUtil.createTestManager("KAKAO-251101");
        userRepository.save(manager);

        FestivalSearchCondForAdmin cond = new FestivalSearchCondForAdmin(null, FestivalState.APPROVED, FestivalType.FESTAPICK);

        Festival festival1 = createApprovedCustomFestivalAndSave(manager);
        createCustomFestivalAndSave(manager);
        createFestivalAndSaveByAreaCode(15);

        //when
        Pageable pageable = PageRequest.of(0, 5);
        Page<Festival> festivals = qFestivalRepository.findByStateAndTitleLikeAndType(cond, pageable);

        //then
        assertSoftly(
                softly -> {
                    softly.assertThat(festivals.getTotalElements()).isEqualTo(1);
                    softly.assertThat(festivals).contains(festival1);
                    softly.assertThat(festivals.getContent().getFirst().getState()).isEqualTo(FestivalState.APPROVED);
                    softly.assertThat(festivals.getContent().getFirst().getFestivalType()).isEqualTo(FestivalType.FESTAPICK);
                });
    }

    @Test
    @DisplayName("custom 축제 조회하기")
    void findByType() throws Exception {
        //given
        UserEntity manager = testUtil.createTestManager("KAKAO-251101");
        userRepository.save(manager);

        FestivalSearchCondForAdmin cond = new FestivalSearchCondForAdmin(null, null, FestivalType.FESTAPICK);

        createApprovedCustomFestivalAndSave(manager);
        createCustomFestivalAndSave(manager);
        createCustomFestivalAndSave(manager);

        createFestivalAndSaveByAreaCode(15);
        createFestivalAndSaveByAreaCode(13);

        //when
        Pageable pageable = PageRequest.of(0, 5);
        Page<Festival> festivals = qFestivalRepository.findByStateAndTitleLikeAndType(cond, pageable);

        //then
        assertSoftly(
                softly -> {
                    softly.assertThat(festivals.getTotalElements()).isEqualTo(3);
                    softly.assertThat(festivals.getContent().getFirst().getFestivalType()).isEqualTo(FestivalType.FESTAPICK);
                });
    }


    private Festival createFestivalAndSaveByAreaCode(int areaCode) throws Exception {
        return festivalRepository.save(testUtil.createTestFestivalByAreaCode(areaCode));
    }

    private Festival createApprovedCustomFestivalAndSave(UserEntity user) throws Exception {
        Festival festival = testUtil.createTestFestival(user);
        festival.updateState(FestivalState.APPROVED);
        return festivalRepository.save(festival);
    }

    private Festival createCustomFestivalAndSave(UserEntity user) throws Exception {
        return festivalRepository.save(testUtil.createTestFestival(user));
    }

}