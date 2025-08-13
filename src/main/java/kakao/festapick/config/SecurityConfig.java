package kakao.festapick.config;

import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.global.filter.JWTFilter;
import kakao.festapick.jwt.JWTUtil;
import kakao.festapick.jwt.service.JwtService;
import kakao.festapick.oauth2.resolver.CustomAuthorizationRequestResolver;
import kakao.festapick.oauth2.handler.SocialSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final JwtService jwtService;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth->auth
                .anyRequest().permitAll());

        http.oauth2Login(oauth2->oauth2
                .successHandler(new SocialSuccessHandler(jwtUtil, jwtService))
                .authorizationEndpoint(endPoint->endPoint.authorizationRequestResolver(customAuthorizationRequestResolver)));

        http.exceptionHandling(e->e
                .authenticationEntryPoint((request, response, authException)-> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, authException)-> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }));

        http.sessionManagement(session->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JWTFilter(jwtUtil), LogoutFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
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
