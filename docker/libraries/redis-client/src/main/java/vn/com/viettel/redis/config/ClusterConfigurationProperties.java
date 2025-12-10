package vn.com.viettel.redis.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.data.redis.cluster")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClusterConfigurationProperties {
    List<String> nodes;

}
