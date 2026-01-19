package vn.com.viettel.config.keycloak;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final ExtendedKeycloakProperties properties;

    @Bean
    public Keycloak keycloak() {
        ResteasyClientBuilderImpl clientBuilder = new ResteasyClientBuilderImpl();

        int poolSize = properties.getConnectionPoolSize() > 0 ? properties.getConnectionPoolSize() : 10;
        clientBuilder.connectionPoolSize(poolSize);

        clientBuilder.connectTimeout(10, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);

        return KeycloakBuilder.builder()
                .serverUrl(properties.getAuthServerUrl())
                .realm(properties.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .resteasyClient(clientBuilder.build())
                .build();
    }
}