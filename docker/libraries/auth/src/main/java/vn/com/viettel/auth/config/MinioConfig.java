package vn.com.viettel.auth.config;

import io.minio.MinioClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinioConfig {
    @Value("${minio.baseUrl}")
    String baseUrl;

    @Value("${minio.accessKey}")
    String minioAccessKey;

    @Value("${minio.secretKey}")
    String minioSecretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(baseUrl).credentials(minioAccessKey, minioSecretKey).build();
    }
}
