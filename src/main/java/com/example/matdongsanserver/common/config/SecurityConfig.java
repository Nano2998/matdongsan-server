package com.example.matdongsanserver.common.config;

import com.example.matdongsanserver.domain.auth.filter.JwtFilter;
import com.example.matdongsanserver.domain.auth.handler.JwtAccessDeniedHandler;
import com.example.matdongsanserver.domain.auth.handler.JwtAuthenticationFailEntryPoint;
//import com.example.matdongsanserver.domain.auth.handler.OAuth2SuccessHandler;
import com.example.matdongsanserver.domain.auth.kakao.KakaoMemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KakaoMemberDetailsService kakaoMemberDetailsService;
//    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtFilter customJwtFilter;
    private final JwtAuthenticationFailEntryPoint jwtAuthenticationFailEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 아래 요청들은 시큐리티 검증을 완전히 우회함.
     * .ignoring():
     * 		- 인증 및 권한 검사 없이 접근 가능해 Spring Security의 보안 처리를 완전히 우회함.
     * 		- 주로 정적 리소스에 사용됨.
     * .permitAll():
     * 		- 모든 사용자의 접근을 허용
     * 		- CSRF 보호, XSS 방지 등 Spring Security의 다른 보안 메커니즘은 여전히 적용됨.
     * 		- 주로 특정 API 엔드포인트나 페이지에 사용됨.
     * 		- 보안 검사는 필요하지만 인증은 필요하지 않은 경우에 적합.
     * 	    - 필터는 통과하므로 추가적인 처리 필요.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers( "/css/**",
                        "/images/**",
                        "/js/**",
                        "/favicon.ico",
                        "/fonts/**",
                        " /assets/**",
                        "/error"
                        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors((httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource())));
        http
                .csrf(AbstractHttpConfigurer::disable);
        http
                .formLogin(AbstractHttpConfigurer::disable);
        http
                .httpBasic(AbstractHttpConfigurer::disable);
        http
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http
                .oauth2Login(oAuth2Login -> {
                    oAuth2Login.userInfoEndpoint(userInfoEndpointConfig ->
                    userInfoEndpointConfig.userService(kakaoMemberDetailsService));
                });
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/swagger-ui.html").permitAll()
                        .requestMatchers("/api/swagger-ui/**").permitAll()
                        .requestMatchers("/api/swagger-resources/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().authenticated()
                );
        http
                .addFilterBefore(customJwtFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.authenticationEntryPoint(jwtAuthenticationFailEntryPoint);
                    exceptionHandling.accessDeniedHandler(jwtAccessDeniedHandler);
                });


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
