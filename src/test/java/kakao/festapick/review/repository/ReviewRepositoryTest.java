package kakao.festapick.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.review.domain.Review;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
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
public class ReviewRepositoryTest {

    private static final String identifier = "GOOGLE-1234";

    private final FestivalRequestDto requestDto = new FestivalRequestDto("1234567", "test festival",
            "11",
            "test addr1", "test addr2", "http://asd.test.com/example.jpg", "20250823",
            "20251231");

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FestivalRepository festivalRepository;

    private UserEntity saveUserEntity() {

        return userRepository.save(new UserEntity(identifier,
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE));
    }

    private Festival saveFestival() {

        String overView = "test overview";
        String homepage = "http://asd.test.com";
        return festivalRepository.save(new Festival(requestDto, overView, homepage));
    }

    @Test
    @DisplayName("리뷰 등록 성공 테스트")
    void createReviewSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        Review actual = reviewRepository.save(new Review(userEntity, festival, "testtesttest", 4));

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getContent()).isEqualTo("testtesttest"),
                () -> AssertionsForClassTypes.assertThat(actual.getScore()).isEqualTo(4)
        );
    }

    @Test
    @DisplayName("리뷰 아이디로 리뷰 찾기 성공 테스트")
    void findReviewByIdSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        Review saved = reviewRepository.save(new Review(userEntity, festival, "testtesttest", 4));

        Optional<Review> find = reviewRepository.findByUserIdentifierAndId(userEntity.getIdentifier(),
                saved.getId());

        assertThat(find).isPresent();

        Review actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getContent()).isEqualTo("testtesttest"),
                () -> AssertionsForClassTypes.assertThat(actual.getScore()).isEqualTo(4)
        );
    }

    @Test
    @DisplayName("축제 아이디로 내 리뷰 존재 확인 테스트")
    void findReviewByFestivalIdSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        reviewRepository.save(new Review(userEntity, festival, "testtesttest", 4));

        assertThat(reviewRepository.existsByUserIdAndFestivalId(userEntity.getId(),
                festival.getId())).isTrue();
    }

    @Test
    @DisplayName("내 리뷰 리스트 조회 성공 테스트")
    void findMyReviewSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        Review saved = reviewRepository.save(new Review(userEntity, festival, "testtesttest", 4));

        Page<Review> find = reviewRepository.findByUserIdentifier(userEntity.getIdentifier(),
                PageRequest.of(0, 1));

        Review actual = find.getContent().get(0);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getContent()).isEqualTo("testtesttest"),
                () -> AssertionsForClassTypes.assertThat(actual.getScore()).isEqualTo(4)
        );
    }

    @Test
    @DisplayName("특정 축제의 리뷰 리스트 조회 성공 테스트")
    void findFestivalReviewSuccess() {

        UserEntity userEntity = saveUserEntity();

        Festival festival = saveFestival();

        Review saved = reviewRepository.save(new Review(userEntity, festival, "testtesttest", 4));

        Page<Review> find = reviewRepository.findByFestivalId(festival.getId(),
                PageRequest.of(0, 1));

        Review actual = find.getContent().get(0);

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getContent()).isEqualTo("testtesttest"),
                () -> AssertionsForClassTypes.assertThat(actual.getScore()).isEqualTo(4)
        );
    }
}
