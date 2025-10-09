package kakao.festapick.permission.fmpermission.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.lang.reflect.Field;
import java.util.Optional;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.fmpermission.domain.FMPermission;
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
class FMPermissionRepositoryTest {

    @Autowired private FMPermissionRepository fmPermissionRepository;

    @Autowired private UserRepository userRepository;

    private final TestUtil testUtil = new TestUtil();

    private Long createFMPermissionAndReturnUserId(){
        UserEntity user1 = testUtil.createTestUser("KAKAO-1234");
        userRepository.save(user1);
        fmPermissionRepository.save(new FMPermission(user1, "부산대학교"));
        return user1.getId();
    }

    private FMPermission newFMPermission(UserEntity user) throws Exception {
        FMPermission fmPermission = new FMPermission(user, "부산대학교");
        Field idField = FMPermission.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(fmPermission, 20L);
        return fmPermission;
    }

    void createFMPermissions(){
        UserEntity user1 = testUtil.createTestUser("KAKAO-1234");
        userRepository.save(user1);
        fmPermissionRepository.save(new FMPermission(user1, "부산대학교"));

        UserEntity user2 = testUtil.createTestUser("GOOGLE-1234");
        userRepository.save(user2);
        fmPermissionRepository.save(new FMPermission(user2, "카카오대학교"));
    }

    @Test
    @DisplayName("FMPermission 신청내역을 userId로 조회")
    void findFMPermissionByUserId(){
        //given
        Long userId = createFMPermissionAndReturnUserId();

        //when
        Optional<FMPermission> foundOne = fmPermissionRepository.findByUserId(userId);

        //then
        assertThat(foundOne).isNotEmpty();
        FMPermission actual = foundOne.get();

        assertAll(
                () -> actual.getPermissionState().equals(PermissionState.PENDING),
                () -> actual.getUser().getId().equals(userId),
                () -> actual.getDepartment().equals("부산대학교")
        );
    }

    @Test
    @DisplayName("FMPermission 신청 내역을 id로 삭제")
    void removeFMPermissionByUserId() throws Exception{
        //given
        UserEntity user = testUtil.createTestUser();
        Long id = newFMPermission(user).getId();

        //when
        fmPermissionRepository.removeFMPermissionById(id);

        //then
        Optional<FMPermission> foundOne = fmPermissionRepository.findFMPermissionById(id);
        assertThat(foundOne).isEmpty();
    }

    @Test
    @DisplayName("FMPermission 신청 내역의 존재 여부를 userID로 조회")
    void existsFMPermissionByUserId(){
        //given
        Long userId = createFMPermissionAndReturnUserId();

        //when
        Boolean result = fmPermissionRepository.existsFMPermissionByUserId(userId);

        //then
        assertThat(result).isEqualTo(true);
    }

    @Test
    @DisplayName("FMPermission 신청 내역을 모두 조회")
    void findAllFMPermissionsWithUser(){
        //given
        createFMPermissions();
        Pageable pageable = PageRequest.of(0, 5);

        //when
        Page<FMPermission> pageContent = fmPermissionRepository.findAllFMPermissionsWithUser(pageable);

        //then
        assertAll(
                () -> assertThat(pageContent.getTotalElements()).isEqualTo(2),
                () -> assertThat(pageContent.getContent().getFirst()).isInstanceOf(FMPermission.class)
        );
    }

    @Test
    @DisplayName("FMPermission id를 통한 조회")
    void findFMPermissionById(){
        //given
        UserEntity user = testUtil.createTestUser();
        userRepository.save(user);
        FMPermission saved = fmPermissionRepository.save(new FMPermission(user, "우아한대학교"));

        //when
        Optional<FMPermission> foundOne = fmPermissionRepository.findFMPermissionById(saved.getId());

        //then
        assertThat(foundOne).isNotEmpty();
        FMPermission actual = foundOne.get();

        assertAll(
                () -> assertThat(actual.getUser()).isEqualTo(user),
                () -> assertThat(actual.getDepartment()).isEqualTo("우아한대학교"),
                () -> assertThat(actual.getId()).isEqualTo(saved.getId())
        );
    }

}