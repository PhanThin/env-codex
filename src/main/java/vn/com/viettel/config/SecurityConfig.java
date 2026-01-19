package vn.com.viettel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.com.viettel.auth.config.filter.ApiKeyAuthenticationFilter;
import vn.com.viettel.config.keycloak.KeycloakRoleConverter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${common.ip.restricted.url:}")
    private String ipRestrictedUrl;

    @Value("${common.permission.ignore.url:}")
    private String permissionIgnoreUrl;

    @Value("${app.api-key:null-api-key}")
    private String apiKey;

    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean("customSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable);

        if (!"null-api-key".equals(this.apiKey)) {
            http.addFilterBefore(new ApiKeyAuthenticationFilter(this.apiKey), UsernamePasswordAuthenticationFilter.class);
        }

        http.authorizeHttpRequests(auth -> {
            handleIgnoreUrls(auth);
            handleIpRestrictions(auth);
            auth.requestMatchers("/api/v1/auth/login").permitAll()
                    .requestMatchers("/api/v1/auth/change-password").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .anyRequest().authenticated();
        });

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRoleConverter()))
        );

        return http.build();
    }

    private void handleIgnoreUrls(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        if (permissionIgnoreUrl.trim().isEmpty()) return;
        String[] arrLinkIgnore = permissionIgnoreUrl.split(";");
        for (String str : arrLinkIgnore) {
            String[] parts = str.trim().split(":");
            if (parts.length > 1) {
                auth.requestMatchers(new AntPathRequestMatcher(parts[1].trim(), parts[0].trim().toUpperCase())).permitAll();
            } else {
                auth.requestMatchers(str.trim()).permitAll();
            }
        }
    }

    private void handleIpRestrictions(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        if (ipRestrictedUrl.trim().isEmpty()) return;
        String[] arrUrlIpRestricted = ipRestrictedUrl.split(";");
        for (String string : arrUrlIpRestricted) {
            String[] strConf = string.trim().split("\\|");
            if (strConf.length > 1) {
                String strUrl = strConf[0].trim();
                String[] arrIP = strConf[1].trim().split(",");
                StringBuilder hasIpAddressPermit = new StringBuilder();
                for (int i = 0; i < arrIP.length; i++) {
                    hasIpAddressPermit.append("hasIpAddress('").append(arrIP[i]).append("')");
                    if (i < arrIP.length - 1) hasIpAddressPermit.append(" or ");
                }
                auth.requestMatchers(strUrl).access(new WebExpressionAuthorizationManager(hasIpAddressPermit.toString()));
            }
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}