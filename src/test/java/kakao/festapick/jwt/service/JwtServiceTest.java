package kakao.festapick.jwt.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.component.TokenEncoder;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.domain.RefreshToken;
import kakao.festapick.jwt.repository.RefreshTokenRepository;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.service.OAuth2UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private OAuth2UserService oAuth2UserService;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private CookieComponent cookieComponent;

    @Mock
    private TokenEncoder tokenEncoder;

    @Test
    @DisplayName("리프래시 토큰 저장 성공")
    void saveRefreshTokenSuccess() {

        // given
        String refreshToken = UUID.randomUUID().toString();
        UserEntity userEntity = createUserEntity();

        given(oAuth2UserService.findByIdentifier(any()))
                .willReturn(userEntity);

        given(tokenEncoder.encode(any()))
                .willReturn(refreshToken);

        given(refreshTokenRepository.save(any()))
                .willReturn(new RefreshToken(userEntity,refreshToken));


        // then
        RefreshToken saveRefreshToken = jwtService.saveRefreshToken(userEntity.getIdentifier(), refreshToken);

        // then
        assertSoftly(
                softly -> {
                    softly.assertThat(saveRefreshToken.getToken()).isEqualTo(refreshToken);
                    softly.assertThat(saveRefreshToken.getUser().getIdentifier()).isEqualTo(userEntity.getIdentifier());
                }
        );

        verify(oAuth2UserService).findByIdentifier(any());
        verify(refreshTokenRepository).deleteByUser(any());
        verify(tokenEncoder).encode(any());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("토큰 교환 성공")
    void exchangeTokenSuccess() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setCookies(new Cookie("refreshToken", UUID.randomUUID().toString()));

        given(jwtUtil.validateToken(any(), anyBoolean()))
                .willReturn(true);

        Claims claims = mock(Claims.class);
        given(jwtUtil.getClaims(any())).willReturn(claims);
        given(claims.get("identifier")).willReturn("GOOGLE_123");
        given(claims.get("role")).willReturn("ROLE_USER");

        given(jwtUtil.getClaims(any()))
                .willReturn(claims);

        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        given(jwtUtil.createJWT(any(), any(), any()))
                .willReturn(accessToken, refreshToken);

        UserEntity userEntity = createUserEntity();

        given(refreshTokenRepository.findByUserIdentifier(any()))
                .willReturn(Optional.of(new RefreshToken(userEntity, UUID.randomUUID().toString())));

        given(tokenEncoder.match(any(), any()))
                .willReturn(true);


        given(oAuth2UserService.findByIdentifier(any()))
                .willReturn(userEntity);

        given(refreshTokenRepository.save(any()))
                .willReturn(new RefreshToken(userEntity,refreshToken));

        String setCookieHeader = ResponseCookie.from(refreshToken).build().toString();

        given(cookieComponent.createRefreshToken(any()))
                .willReturn(setCookieHeader);

        // when
        jwtService.exchangeToken(request,response);

        assertSoftly(
                softly -> {
                    softly.assertThat(response.getHeader("Authorization")).isEqualTo("Bearer " + accessToken);
                    softly.assertThat(response.getHeader("Set-Cookie")).isEqualTo(setCookieHeader);
                }
        );
        verify(oAuth2UserService).findByIdentifier(any());
        verify(refreshTokenRepository).deleteByUser(any());
        verify(refreshTokenRepository).save(any());
        verify(jwtUtil).getClaims(any());
        verify(jwtUtil).validateToken(any(), anyBoolean());
        verify(jwtUtil, times(2)).createJWT(any(), any(), any());
        verifyNoMoreInteractions(oAuth2UserService, jwtUtil, refreshTokenRepository, cookieComponent);
    }

    @Test
    @DisplayName("토큰 교환 실패 - 쿠키가 없음")
    void exchangeTokenFailure() {

        // given

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when & then
        assertThatThrownBy(()->jwtService.exchangeToken(request,response))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("쿠키가 존재하지 않습니다.");
        verifyNoMoreInteractions(oAuth2UserService, jwtUtil, refreshTokenRepository, cookieComponent);

    }

    @Test
    @DisplayName("토큰 교환 실패 - 쿠키는 존재하지만 리프래시 토큰이 없음")
    void exchangeTokenFailure2() {

        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setCookies(new Cookie("JSESSIONID",  UUID.randomUUID().toString()));

        // when & then
        assertThatThrownBy(()->jwtService.exchangeToken(request,response))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("리프래시 토큰이 존재하지 않습니다.");
        verifyNoMoreInteractions(oAuth2UserService, jwtUtil, refreshTokenRepository, cookieComponent);
    }

    @Test
    @DisplayName("토큰 교환 실패 - 리프래시 토큰이 유효하지 않을 때")
    void exchangeTokenFailure3() {

        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setCookies(new Cookie("refreshToken",  UUID.randomUUID().toString()));

        given(jwtUtil.validateToken(any(), anyBoolean()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(()->jwtService.exchangeToken(request,response))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("리프래시 토큰이 유효하지 않습니다.");
        verify(jwtUtil).validateToken(any(), anyBoolean());
        verifyNoMoreInteractions(oAuth2UserService, jwtUtil, refreshTokenRepository, cookieComponent);
    }

    private UserEntity createUserEntity() {
        return new UserEntity(1L, "GOOGLE_1234", "example@gmail.com",
                "exampleName", UserRoleType.USER, SocialType.GOOGLE);
    }

}