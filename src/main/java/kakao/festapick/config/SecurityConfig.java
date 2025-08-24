package kakao.festapick.config;

import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.component.CookieComponent;
import kakao.festapick.global.filter.CustomLogoutFilter;
import kakao.festapick.global.filter.CustomLogoutFilterForAdminPage;
import kakao.festapick.global.filter.JWTFilter;
import kakao.festapick.global.filter.JWTFilterForAdminPage;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.service.JwtService;
import kakao.festapick.oauth2.handler.SocialSuccessHandler;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final JwtService jwtService;
    private final CookieComponent cookieComponent;
    private final OAuth2UserService oAuth2UserService;
    @Value("${spring.front.domain}")
    private String frontDomain;
    @Value("${spring.backend.domain}")
    private String backendDomain;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth->auth
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/wishes/**").authenticated()
                .requestMatchers("/api/reviews/**").authenticated()
                .requestMatchers("/admin/**").hasRole(UserRoleType.ADMIN.name())
                .anyRequest().permitAll());

        http.oauth2Login(oauth2->oauth2
                .userInfoEndpoint((userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(oAuth2UserService)))
                .successHandler(new SocialSuccessHandler(jwtUtil, jwtService, cookieComponent, frontDomain)));

        http.exceptionHandling(e->e
                .authenticationEntryPoint((request, response, authException)-> {
                    if (request.getRequestURI().startsWith("/admin")) response.sendRedirect("/login");
                    else response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, authException)-> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }));

        http.sessionManagement(session->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JWTFilter(jwtUtil,oAuth2UserService), LogoutFilter.class);

        http.addFilterBefore(new JWTFilterForAdminPage(jwtUtil,oAuth2UserService,cookieComponent), JWTFilter.class);

        http.addFilterAt(new CustomLogoutFilter(jwtUtil, jwtService, cookieComponent), LogoutFilter.class);
        http.addFilterAfter(new CustomLogoutFilterForAdminPage(jwtUtil, cookieComponent),CustomLogoutFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(frontDomain,backendDomain));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
