package vn.com.viettel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import vn.com.viettel.core.config.EnableJasypt;

@SpringBootApplication
@EnableJasypt(key = "sxQG0HbwfOnt1genO7VG3hWX82kk2vUpzx9GuLLna6n7zKVn2lvCis/nB0oUwA==")
@EnableFeignClients
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

}
