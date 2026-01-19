package vn.com.viettel.config.keycloak;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import vn.com.viettel.auth.config.KeycloakAdminClientProperties;

@Component
@ConfigurationProperties(prefix = "keycloak-admin-client")
@Data
@Primary
@EqualsAndHashCode(callSuper = true)
public class ExtendedKeycloakProperties extends KeycloakAdminClientProperties {
    private String clientSecret;
}