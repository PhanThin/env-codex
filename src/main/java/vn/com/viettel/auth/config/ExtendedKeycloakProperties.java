package vn.com.viettel.auth.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak-admin-client")
@Data
@Primary // Đảm bảo Spring ưu tiên Bean này hơn Bean từ thư viện nội bộ
@EqualsAndHashCode(callSuper = true)
public class ExtendedKeycloakProperties extends KeycloakAdminClientProperties {
    // Bổ sung các trường còn thiếu mà thư viện nội bộ chưa có
    private String clientSecret;
}