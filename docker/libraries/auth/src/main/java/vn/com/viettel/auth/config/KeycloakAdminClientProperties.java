package vn.com.viettel.auth.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak-admin-client")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakAdminClientProperties {
    String realm;
    String clientId;
    String authServerUrl;
    String masterUsername;
    String masterPassword;
    int connectionPoolSize;
}
