package ai.codegeist.server.auth;

import ai.codegeist.server.HealthController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * First Codegeist Cloud API security boundary.
 *
 * <p>The server is not an OAuth2 authorization server in this slice. It only
 * protects Codegeist-owned API routes and can validate JWT bearer tokens when a
 * deployment supplies a {@link JwtDecoder} through Spring Boot resource-server
 * properties. Without a decoder, API routes still require authentication but no
 * bearer-token mechanism is enabled, keeping the default server fail-closed while
 * tests can use Spring Security's MockMvc JWT support.
 */
@Configuration
class CodegeistServerSecurityConfiguration {

    static final String API_PATH_PATTERN = "/api/**";

    @Bean
    SecurityFilterChain codegeistServerSecurityFilterChain(HttpSecurity http, ObjectProvider<JwtDecoder> jwtDecoder)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HealthController.HEALTH_PATH).permitAll()
                        .requestMatchers(API_PATH_PATTERN).authenticated()
                        .anyRequest().denyAll());

        JwtDecoder decoder = jwtDecoder.getIfAvailable();
        if (decoder != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(decoder)));
        }

        return http.build();
    }
}
