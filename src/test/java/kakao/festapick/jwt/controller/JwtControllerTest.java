package kakao.festapick.jwt.controller;

import jakarta.servlet.http.Cookie;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.jwt.util.JwtUtil;
import kakao.festapick.jwt.service.JwtService;
import kakao.festapick.jwt.util.TokenType;
import kakao.festapick.mockuser.WithCustomMockUser;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.repository.UserRepository;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String identifier = "GOOGLE_1234";
    @Autowired
    private TestUtil testUtil;

    @Test
    @DisplayName("토큰 교환 성공")
    @WithCustomMockUser(identifier = identifier, role = "USER_ROLE")
    void exchangeTokenSuccess() throws Exception {

        UserEntity userEntity = saveUserEntity();

        String refreshToken = jwtUtil.createJWT(userEntity.getIdentifier(), userEntity.getRoleType().name(), TokenType.REFRESH_TOKEN);

        jwtService.saveRefreshToken(userEntity.getIdentifier(), refreshToken);

        mockMvc.perform(post("/api/jwt/exchange")
                .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken")))
                .andExpect(header().string("Authorization", containsString("Bearer")));

    }

    @Test
    @DisplayName("토큰 교환 실패 - 쿠키가 존재하지 않음")
    @WithCustomMockUser(identifier = identifier, role = "USER_ROLE")
    void exchangeTokenFailure() throws Exception {

        mockMvc.perform(post("/api/jwt/exchange"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ExceptionCode.COOKIE_NOT_EXIST.getErrorMessage()));
    }

    @Test
    @DisplayName("토큰 교환 실패 - 리프래시 토큰이 존재하지 않음")
    @WithCustomMockUser(identifier = identifier, role = "USER_ROLE")
    void exchangeTokenFailure2() throws Exception {

        Cookie cookie = new Cookie("cookie", null);

        mockMvc.perform(post("/api/jwt/exchange")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ExceptionCode.REFRESH_TOKEN_NOT_EXIST.getErrorMessage()));
    }

    @Test
    @DisplayName("토큰 교환 실패 - 리프래시 토큰이 만료되었음")
    @WithCustomMockUser(identifier = identifier, role = "USER_ROLE")
    void exchangeTokenFailure3() throws Exception {

        UserEntity userEntity = saveUserEntity();

        String refreshToken = jwtUtil.createJWT(userEntity.getIdentifier(), userEntity.getRoleType().name(), TokenType.REFRESH_TOKEN, 0L);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        mockMvc.perform(post("/api/jwt/exchange")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ExceptionCode.INVALID_REFRESH_TOKEN.getErrorMessage()));
    }

    private UserEntity saveUserEntity() {
        return userRepository.save(testUtil.createTestUser(identifier));
    }

}
