package vn.com.viettel.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return Map.of("message", "Không tìm thấy thông tin người dùng");
        }
        // JwtAuthenticationToken đã map preferred_username vào principal name
        return Map.of(
            "username", jwt.getClaimAsString("preferred_username"),
            "fullName", jwt.getClaimAsString("name"), // Claim name thường chứa full name
            "roles", jwt.getClaimAsMap("realm_access").get("roles")
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('admin')") // Kiểm tra Role 'admin' (Mapper đã thêm tiền tố ROLE_)
    public String adminPage() {
        return "Chào sếp! Đây là trang dành riêng cho ADMIN.";
    }
}