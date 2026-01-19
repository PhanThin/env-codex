package vn.com.viettel.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private Password password = new Password();
    private BruteForce bruteForce = new BruteForce();

    @Data
    public static class Password {
        private int expiryDays;
    }

    @Data
    public static class BruteForce {
        private int maxFailures;
        private int lockDurationMinutes;
    }
}