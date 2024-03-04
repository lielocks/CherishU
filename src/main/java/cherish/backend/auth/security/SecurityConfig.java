package cherish.backend.auth.security;

import cherish.backend.auth.jwt.JwtTokenProvider;
import cherish.backend.auth.jwt.filter.JwtAuthenticationFilter;
import cherish.backend.auth.jwt.filter.JwtExceptionFilter;
import cherish.backend.common.config.cors.CorsProperties;
import cherish.backend.common.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.PrintWriter;
import java.util.Set;

@EnableConfigurationProperties(CorsProperties.class)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final CorsProperties corsProperties;
    // 현재 화이트 리스트 모두 열어 놓음
    private static final String[] PUBLIC_WHITELIST = {
            "/public/**", "/test/**","/monitor/**"
    };

    // 비밀번호 변환 알고리즘
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain config(HttpSecurity http) throws Exception {
        return http
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedEntryPoint)
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .authorizeHttpRequests(authorize ->
                    authorize
                    .shouldFilterAllDispatcherTypes(false)
                    .requestMatchers(CorsUtils::isPreFlightRequest)
                    .permitAll()
                    .requestMatchers(PUBLIC_WHITELIST)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class)
                .build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        Set<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (allowedOrigins != null) {
            allowedOrigins.forEach(config::addAllowedOrigin);
        }
        Set<String> allowedMethods = corsProperties.getAllowedMethods();
        if (allowedMethods != null) {
            allowedMethods.forEach(config::addAllowedMethod);
        }
        Set<String> allowedHeaders = corsProperties.getAllowedHeaders();
        if (allowedHeaders != null) {
            allowedHeaders.forEach(config::addAllowedHeader);
        }

//        corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);
//        corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
//        corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private final AuthenticationEntryPoint unauthorizedEntryPoint = (request, response, authException) -> {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        String json = new ObjectMapper().writeValueAsString(new ErrorResponseDto("Authentication is required."));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    };
}
