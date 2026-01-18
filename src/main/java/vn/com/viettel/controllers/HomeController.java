package vn.com.viettel.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "Chào bạn, khách lạ!";
        }
        // Trả về tên và email lấy từ Keycloak
        return String.format("<h1>Đăng nhập thành công!</h1>" +
                             "Chào mừng, <b>%s</b>!<br>" +
                             "Email của bạn là: %s",
                             principal.getAttribute("name"),
                             principal.getAttribute("email"));
    }
}