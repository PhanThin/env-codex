package vn.com.viettel.auth.config;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl; // Sử dụng Impl cho Jakarta
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak-admin-client.authServerUrl}")
    private String serverUrl;

    @Value("${keycloak-admin-client.realm}")
    private String realm;

    @Value("${keycloak-admin-client.clientId}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${keycloak-admin-client.connectionPoolSize:10}")
    private int connectionPoolSize;

    @Bean
    public Keycloak keycloak() {
        // Khởi tạo thông qua lớp Implementation cụ thể
        ResteasyClientBuilderImpl clientBuilder = new ResteasyClientBuilderImpl();
        clientBuilder.connectionPoolSize(connectionPoolSize);

        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(clientBuilder.build())
                .build();
    }
}