package vn.com.viettel.auth.config;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.keycloak.adapters.authorization.integration.jakarta.ServletPolicyEnforcerFilter;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import vn.com.viettel.auth.config.filter.ApiKeyAuthenticationFilter;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;


@Configuration
@EnableWebSecurity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GlobalSecurityConfiguration {
    final static Logger LOGGER = LoggerFactory.getLogger(GlobalSecurityConfiguration.class);
    final static String MINIO_BUCKET = "keycloak-adapter-config";
    final ConcurrentHashMap<String, ServletPolicyEnforcerFilter> cache = new ConcurrentHashMap<>();
    @Value("${common.ip.restricted.url}")
    String ipRestrictedUrl;
    @Value("${common.permission.ignore.url}")
    String permissionIgnoreUrl;
    @Value("${app.api-key:null-api-key}")
    String apiKey;
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;
    @Value("${spring.application.name}")
    String applicationName;
    final String[] resource = {"/v3/api-docs", "/v3/api-docs/**", "/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html**", "/webjars/**", "/swagger-ui/**", "/favicon.ico"};
    @Autowired(required = false)
    UserDetailsService userDetailsService;
    @Autowired
    MinioClient minioClient;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(requests -> requests.requestMatchers(resource).permitAll())
//                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
//                .addFilterAfter(createPolicyEnforcerFilter(), BearerTokenAuthenticationFilter.class);

        // Cau hinh restricted IP via URL
        String strUrlIpRestricted = ipRestrictedUrl;
        if (!strUrlIpRestricted.trim().isEmpty()) {
            String[] arrUrlIpRestricted = strUrlIpRestricted.split(";");
            for (String string : arrUrlIpRestricted) {
                String[] strConf = string.trim().split("\\|");
                if (strConf.length > 1) {
                    String strUrl = strConf[0].trim();
                    String strIp = strConf[1].trim();
                    if (!strUrl.isEmpty() && !strIp.isEmpty()) {
                        String[] arrIP = strIp.split(",");
                        StringBuilder hasIpAddressPermit = new StringBuilder();
                        for (String ip : arrIP) {
                            hasIpAddressPermit.append("hasIpAddress('").append(ip).append("')").append(" or ");
                        }
                        http.authorizeRequests(requests -> requests.requestMatchers(strUrl).access(String.valueOf(new WebExpressionAuthorizationManager(hasIpAddressPermit.toString()))).anyRequest());
                    }
                }
            }
        }

        // Cau hinh ignore URL bypass authentication
        String strPermission = permissionIgnoreUrl;
        if (!strPermission.trim().isEmpty()) {
            String[] arrLinkIgnore = strPermission.split(";");
            for (String string : arrLinkIgnore) {
                String strLinkIg = string.trim();
                String[] strSleep = strLinkIg.split(":");
                if (strSleep.length > 1) {
                    String strMethod = strSleep[0].trim();
                    String strUrl = strSleep[1].trim();
                    if (!strMethod.isEmpty() && !strUrl.isEmpty()) {
                        switch (strMethod.toUpperCase()) {
                            case "GET":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.GET.name())).permitAll());
                                break;
                            case "HEAD":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.HEAD.name())).permitAll());
                                break;
                            case "POST":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.POST.name())).permitAll());
                                break;
                            case "PUT":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.PUT.name())).permitAll());
                                break;
                            case "PATCH":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.PATCH.name())).permitAll());
                                break;
                            case "DELETE":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.DELETE.name())).permitAll());
                                break;
                            case "OPTIONS":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.OPTIONS.name())).permitAll());
                                break;
                            case "TRACE":
                                http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher(strUrl, HttpMethod.TRACE.name())).permitAll());
                                break;
                            default:
                                http.csrf(csrf -> csrf.ignoringRequestMatchers(strLinkIg)).authorizeHttpRequests(requests -> requests.requestMatchers((strUrl)).permitAll());
                                break;
                        }
                    } else {
                        http.csrf(csrf -> csrf.ignoringRequestMatchers(strLinkIg)).authorizeHttpRequests(requests -> requests.requestMatchers((strLinkIg)).permitAll());
                    }
                } else {
                    http.csrf(csrf -> csrf.ignoringRequestMatchers(strLinkIg)).authorizeHttpRequests(requests -> requests.requestMatchers((strLinkIg)).permitAll());
                }
            }
        }
        http.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());
        if ("null-api-key".equals(this.apiKey)) {
            LOGGER.info("Key app.api-key was not set, ApiKeyAuthentication will not supported");
        } else {
            http.addFilterBefore(new ApiKeyAuthenticationFilter(this.apiKey), UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }

//    private ServletPolicyEnforcerFilter createPolicyEnforcerFilter() {
//        if (!cache.containsKey(applicationName)) {
//            String clientObject = String.format("%s.json", applicationName);
//            LOGGER.info("Cache miss, load keycloak adapter config from minio/{}/{}", MINIO_BUCKET, clientObject);
//            try (InputStream is = minioClient.getObject(GetObjectArgs.builder().bucket(MINIO_BUCKET).object(clientObject).build())) {
//                PolicyEnforcerConfig config = JsonSerialization.readValue(is, PolicyEnforcerConfig.class);
//                cache.put(applicationName, new ServletPolicyEnforcerFilter(request -> config));
//            } catch (Exception e) {
//                throw new IllegalStateException(String.format("Unable to load: %s/%s", MINIO_BUCKET, clientObject), e);
//            }
//        }
//        return cache.get(applicationName);
//    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        if (userDetailsService != null) {
            DaoAuthenticationProvider daoAP = new DaoAuthenticationProvider();
            daoAP.setUserDetailsService(userDetailsService);
            daoAP.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
            auth.authenticationProvider(daoAP);
        }
    }
}
