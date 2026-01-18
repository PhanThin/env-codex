package vn.com.viettel.auth.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        // 1. Cấu hình Connection Pool (Quản lý tái sử dụng kết nối)
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // Tối đa 100 kết nối tổng cộng
        connectionManager.setDefaultMaxPerRoute(20); // Tối đa 20 kết nối cho mỗi host (Keycloak)

        // 2. Cấu hình Timeout (Đơn vị: miliseconds)
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)      // Thời gian chờ thiết lập kết nối (5s)
                .setSocketTimeout(10000)      // Thời gian chờ phản hồi dữ liệu (10s)
                .setConnectionRequestTimeout(5000) // Thời gian chờ lấy kết nối từ pool (5s)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}