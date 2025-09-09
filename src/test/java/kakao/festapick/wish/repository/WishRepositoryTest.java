package kakao.festapick.wish.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import kakao.festapick.wish.domain.Wish;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
public class WishRepositoryTest {

    private final TestUtil testUtil = new TestUtil();

    private static final String identifier = "GOOGLE-1234";

    private final FestivalRequestDto requestDto = new FestivalRequestDto("1234567", "test festival",
            11, "test addr1", "test addr2", "http://asd.test.com/example.jpg",
            testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));

    @Autowired
    private WishRepository wishRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FestivalRepository festivalRepository;

    private UserEntity saveUserEntity() {

        return userRepository.save(testUtil.createTestUser(identifier));
    }

    private Festival saveFestival() {

        String overView = "test overview";
        String homepage = "http://asd.test.com";
        return festivalRepository.save(new Festival(requestDto, overView, homepage));
    }

    @Test
    @DisplayName("위시 등록 성공 테스트")
    void createWishSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        Wish actual = wishRepository.save(new Wish(userEntity, festival));

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival)
        );
    }

    @Test
    @DisplayName("위시 아이디로 위시 찾기 성공 테스트")
    void findWishByIdSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        Wish saved = wishRepository.save(new Wish(userEntity, festival));

        Optional<Wish> find = wishRepository.findByUserIdentifierAndId(userEntity.getIdentifier(),
                saved.getId());

        assertThat(find).isPresent();

        Wish actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival)
        );
    }

    @Test
    @DisplayName("축제 아이디로 위시 찾기 성공 테스트")
    void findWishByFestivalIdSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        wishRepository.save(new Wish(userEntity, festival));

        Optional<Wish> find = wishRepository.findByUserIdentifierAndFestivalId(userEntity.getIdentifier(),
                festival.getId());

        assertThat(find).isPresent();

        Wish actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival)
        );
    }

    @Test
    @DisplayName("내 위치 리스트 조회 성공 테스트")
    void findMyWishSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        wishRepository.save(new Wish(userEntity, festival));

        Page<Wish> find = wishRepository.findByUserIdentifier(userEntity.getIdentifier(),
                PageRequest.of(0, 1));

        Wish actual = find.getContent().get(0);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival)
        );
    }

}
