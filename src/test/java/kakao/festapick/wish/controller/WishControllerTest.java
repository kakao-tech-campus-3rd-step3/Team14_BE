package kakao.festapick.wish.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestSecurityContextHolderInjection;
import kakao.festapick.util.TestUtil;
import kakao.festapick.wish.domain.Wish;
import kakao.festapick.wish.repository.WishRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WishControllerTest {

    private static final String identifier = "GOOGLE_1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private WishRepository wishRepository;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("위시 등록 성공")
    void createWishSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();

        mockMvc.perform(post(String.format("/api/festivals/%s/wishes", festival.getId())))
                .andExpect(status().isCreated());

        Optional<Wish> find = wishRepository.findByUserIdentifierAndFestivalId(identifier,
                festival.getId());
        assertThat(find).isPresent();
        Wish actual = find.get();

        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival()).isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getUser()).isEqualTo(userEntity)
        );

    }

    @Test
    @DisplayName("위시 등록 실패 (없는 축제에 대한 등록)")
    void createWishFail() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        mockMvc.perform(post(String.format("/api/festivals/%s/wishes", 999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("위시 조회 성공")
    void getWishesSuccess() throws Exception {
        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());

        mockMvc.perform(get("/api/wishes/my"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("위시 삭제 성공")
    void removeWishSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        TestSecurityContextHolderInjection.inject(userEntity.getId(), userEntity.getRoleType());
        Festival festival = saveFestival();

        Wish target = wishRepository.save(new Wish(userEntity, festival));

        mockMvc.perform(delete(String.format("/api/wishes/%s", target.getId())))
                .andExpect(status().isNoContent());

        Optional<Wish> find = wishRepository.findByUserIdentifierAndFestivalId(identifier, festival.getId());
        assertThat(find).isEmpty();
    }

    private UserEntity saveUserEntity() {
        return userRepository.save(testUtil.createTestUser(identifier));
    }

    private Festival saveFestival() throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                11, "test area1", "test area2", "http://asd.example.com/test.jpg",
                testUtil.toLocalDate("20250823"), testUtil.toLocalDate("20251231"));
        Festival festival = new Festival(festivalRequestDto, testUtil.createTourDetailResponse());

        return festivalRepository.save(festival);
    }

}
