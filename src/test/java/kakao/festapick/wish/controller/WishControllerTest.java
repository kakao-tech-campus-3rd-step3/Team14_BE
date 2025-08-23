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
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
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

    @Test
    @DisplayName("위시 등록 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void createWishSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();

        mockMvc.perform(post(String.format("/api/festivals/%s/wishes", festival.getId())))
                .andExpect(status().isCreated());

        Optional<Wish> find = wishRepository.findByUserIdentifierAndFestivalId(identifier,
                festival.getId());
        assertThat(find).isPresent();
        Wish actual = find.get();
        assertAll(
                () -> AssertionsForClassTypes.assertThat(actual.getId()).isNotNull(),
                () -> AssertionsForClassTypes.assertThat(actual.getFestival())
                        .isEqualTo(festival),
                () -> AssertionsForClassTypes.assertThat(actual.getUser())
                        .isEqualTo(userEntity)
        );
    }

    @Test
    @DisplayName("위시 등록 실패 (없는 축제에 대한 등록)")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void createWishFail() throws Exception {

        mockMvc.perform(post(String.format("/api/festivals/%s/wishes", 999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("위시 조회 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void getWishesSuccess() throws Exception {

        mockMvc.perform(get("/api/wishes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("위시 삭제 성공")
    @WithCustomMockUser(identifier = identifier, role = "ROLE_USER")
    void removeWishSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();
        Festival festival = saveFestival();

        Wish target = wishRepository.save(new Wish(userEntity, festival));

        mockMvc.perform(delete(String.format("/api/wishes/%s", target.getId())))
                .andExpect(status().isNoContent());

        Optional<Wish> find = wishRepository.findByUserIdentifierAndFestivalId(identifier,
                festival.getId());
        assertThat(find).isEmpty();
    }

    private UserEntity saveUserEntity() {

        return userRepository.save(new UserEntity(identifier,
                "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE));
    }

    private Festival saveFestival() {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("12345", "example title",
                "11", "test area1", "test area2", "http://asd.example.com/test.jpg", "20250823",
                "20251231");
        Festival festival = new Festival(festivalRequestDto, "http://asd.example.com",
                "testtesttest");

        return festivalRepository.save(festival);
    }
}
