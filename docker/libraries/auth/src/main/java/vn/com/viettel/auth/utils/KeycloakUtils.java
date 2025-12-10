package vn.com.viettel.auth.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Component;
import vn.com.viettel.auth.config.KeycloakAdminClientProperties;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakUtils {

    final KeycloakAdminClientProperties properties;

    public Keycloak getKeycloakInstance() {
        return Keycloak.getInstance(
                properties.getAuthServerUrl(),
                properties.getRealm(),
                properties.getMasterUsername(),
                properties.getMasterPassword(),
                properties.getClientId());
    }
}
