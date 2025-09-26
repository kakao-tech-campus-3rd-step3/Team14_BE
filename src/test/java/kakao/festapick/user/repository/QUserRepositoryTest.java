package kakao.festapick.user.repository;

import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Import(QUserRepository.class)
class QUserRepositoryTest {


    @Autowired
    private QUserRepository qUserRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String identifier = "GOOGLE-1234";

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("회원 검색 동적 쿼리")
    void findByIdentifierOrUserEmail() {

        UserEntity userEntity = saveUserEntity();

        Page<UserEntity> result1 = qUserRepository.findByIdentifierOrUserEmail(
                new UserSearchCond(null, "example", null), PageRequest.of(0, 1)
        );

        Page<UserEntity> result2 = qUserRepository.findByIdentifierOrUserEmail(
                new UserSearchCond("GOOGLE", "example", null), PageRequest.of(0, 1)
        );

        Page<UserEntity> result3 = qUserRepository.findByIdentifierOrUserEmail(
                new UserSearchCond("GOOGLE", "example", UserRoleType.USER), PageRequest.of(0, 1)
        );


        UserEntity userEntity1 = result1.getContent().get(0);
        UserEntity userEntity2 = result2.getContent().get(0);
        UserEntity userEntity3 = result3.getContent().get(0);

        assertThat(userEntity1.getId()).isEqualTo(userEntity2.getId()).isEqualTo(userEntity3.getId());


    }

    private UserEntity saveUserEntity() {

        return userRepository.save(testUtil.createTestUser(identifier));
    }
}
