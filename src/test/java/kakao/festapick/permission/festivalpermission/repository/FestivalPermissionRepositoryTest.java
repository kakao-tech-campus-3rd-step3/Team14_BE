package kakao.festapick.permission.festivalpermission.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FestivalPermissionRepositoryTest {

    @Autowired
    private FestivalPermissionRepository festivalPermissionRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private UserRepository userRepository;

    private final TestUtil testUtil = new TestUtil();

    private UserEntity createUserAndReturn(){
        return userRepository.save(testUtil.createTestUser());
    }

    private UserEntity createSecondUser(){
        return userRepository.save(testUtil.createTestUser("KAKAO-9876"));
    }

    private Festival createFestivalAndReturn(){
        return festivalRepository.save(testUtil.createTourApiTestFestival());
    }

    private FestivalPermission createFestivalPermissionAndReturn(UserEntity user, Festival festival){
        return festivalPermissionRepository.save(new FestivalPermission(user, festival));
    }

    @Test
    void findFestivalPermissionsByUserIdWithFestival() {

        //given
        UserEntity user = createUserAndReturn();
        Festival festival1 = createFestivalAndReturn();
        Festival festival2 = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user, festival1);
        createFestivalPermissionAndReturn(user, festival2);
        Pageable pageable = PageRequest.of(0, 5);

        //when
        Page <FestivalPermission> pageContent = festivalPermissionRepository.findFestivalPermissionsByUserIdWithFestival(user.getId(), pageable);

        //then
        assertAll(
                () -> assertThat(pageContent.getContent().size()).isEqualTo(2),
                () -> assertThat(pageContent.getContent().getFirst().getUser()).isEqualTo(user),
                () -> assertThat(pageContent.getContent().stream().anyMatch(p -> p.getFestival().equals(festival2))).isEqualTo(true)
        );

    }

    @Test
    @DisplayName("userId와 Festival Id로 존재 여부 확인 - 중복 방지를 위함")
    void existsByUserIdAndFestivalId() {
        //given
        UserEntity user = createUserAndReturn();
        Festival festival = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user, festival);

        //when
        Boolean result = festivalPermissionRepository.existsByUserIdAndFestivalId(user.getId(), festival.getId());

        //then
        assertThat(result).isEqualTo(true);
    }

    @Test
    void findByIdAndUserId() {

        //given
        UserEntity user = createUserAndReturn();
        Festival festival  = createFestivalAndReturn();
        FestivalPermission festivalPermission = createFestivalPermissionAndReturn(user, festival);

        //when
        Optional<FestivalPermission> foundOne = festivalPermissionRepository.findByIdAndUserId(festivalPermission.getId(), user.getId());

        //then
        assertThat(foundOne).isNotEmpty();
        FestivalPermission actual = foundOne.get();
        assertAll(

                () -> assertThat(actual.getFestival()).isEqualTo(festival),
                () -> assertThat(actual.getUser()).isEqualTo(user)
        );
    }

    @Test
    @DisplayName("모든 FestivalPermission을 조회")
    void findAllFestivalPermission() {
        //given
        UserEntity user1 = createUserAndReturn();
        Festival festival1 = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user1, festival1);

        UserEntity user2 = createSecondUser();
        Festival festival2 = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user2, festival2);
        Pageable pageable = PageRequest.of(0, 5);

        //when
        Page<FestivalPermission> pagedContent = festivalPermissionRepository.findAllFestivalPermission(pageable);

        //then
        assertAll(
                () -> assertThat(pagedContent.getContent().size()).isEqualTo(2),
                () -> assertThat(pagedContent.getContent().stream().anyMatch(p -> p.getUser().equals(user1))).isEqualTo(true),
                () -> assertThat(pagedContent.getContent().stream().anyMatch(p -> p.getUser().equals(user1))).isEqualTo(true)
        );
    }

    @Test
    @DisplayName("userId를 통해 존재하는 FestivalPermission 조회 - user 삭제를 위함")
    void findByUserId() {
        //given
        UserEntity user = createUserAndReturn();
        Festival festival1 = createFestivalAndReturn();
        Festival festival2 = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user, festival1);
        createFestivalPermissionAndReturn(user, festival2);

        //when
        List<FestivalPermission> result = festivalPermissionRepository.findByUserId(user.getId());

        //then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(result.getFirst().getUser()).isEqualTo(user)
        );
    }

    @Test
    @DisplayName("Festival Id를 통해 존재하는 FestivalPermission 조회 - Festival 삭제를 위함")
    void findByFestivalId() {
        //given
        UserEntity user1 = createUserAndReturn();
        Festival festival = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user1, festival);

        UserEntity user2 = createSecondUser();
        createFestivalPermissionAndReturn(user2, festival);

        //when
        List<FestivalPermission> result = festivalPermissionRepository.findByFestivalId(festival.getId());

        //then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(result.getFirst().getFestival()).isEqualTo(festival)
        );
    }

    @Test
    @DisplayName("id를 통해 FestivalPermission 단건 삭제")
    void deleteById() {
        //given
        UserEntity user1 = createUserAndReturn();
        Festival festival = createFestivalAndReturn();
        FestivalPermission festivalPermission = createFestivalPermissionAndReturn(user1, festival);

        //when
        festivalPermissionRepository.deleteById(festivalPermission.getId());

        //then
        Optional<FestivalPermission> result = festivalPermissionRepository.findByIdWithFestival(festivalPermission.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("userId를 통해 존재하는 FestivalPermission 삭제 - user 삭제를 위함")
    void deleteByUserId() {

        //given
        UserEntity user = createUserAndReturn();
        Festival festival1 = createFestivalAndReturn();
        Festival festival2 = createFestivalAndReturn();
        createFestivalPermissionAndReturn(user, festival1);
        createFestivalPermissionAndReturn(user, festival2);

        //when
        festivalPermissionRepository.deleteByUserId(user.getId());

        //then
        List<FestivalPermission> result = festivalPermissionRepository.findByUserId(user.getId());
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("FestivalId를 통해 존재하는 FestivalPermission 삭제 - festival 삭제를 위함")
    void deleteByFestivalId() {

        //given
        UserEntity user = createUserAndReturn();
        Festival festival = createFestivalAndReturn();

        UserEntity user2 = createSecondUser();
        createFestivalPermissionAndReturn(user, festival);
        createFestivalPermissionAndReturn(user2, festival);

        //when
        festivalPermissionRepository.deleteByFestivalId(festival.getId());

        //then
        List<FestivalPermission> result = festivalPermissionRepository.findByFestivalId(festival.getId());
        assertThat(result.size()).isEqualTo(0);
    }

}