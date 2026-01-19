package vn.com.viettel;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import vn.com.viettel.core.config.EnableJasypt;

@SpringBootApplication
@EnableJasypt(key = "sxQG0HbwfOnt1genO7VG3hWX82kk2vUpzx9GuLLna6n7zKVn2lvCis/nB0oUwA==")
@EnableFeignClients
public class ServiceApplication {

    public static void main(String[] args) {
        // Nạp file .env
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Không báo lỗi nếu thiếu file (ví dụ trên Prod)
                .load();

        // Đưa các biến từ .env vào System Properties để Spring Boot đọc được qua ${...}
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(ServiceApplication.class, args);
    }

}
