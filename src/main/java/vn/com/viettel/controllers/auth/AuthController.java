package vn.com.viettel.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.viettel.services.auth.AuthService;
import vn.com.viettel.dto.auth.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API liên quan đến xác thực, cấp phát Token và quản lý mật khẩu")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Gửi username và password để nhận về bộ Access Token và Refresh Token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "401", description = "Tên đăng nhập hoặc mật khẩu không chính xác"),
            @ApiResponse(responseCode = "403", description = "Tài khoản bị khóa hoặc hết hạn mật khẩu")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) throws Exception {
        LoginResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Đăng xuất",
            description = "Vô hiệu hóa Refresh Token và kết thúc phiên làm việc."
    )
    @ApiResponse(responseCode = "200", description = "Đăng xuất thành công hoặc Token đã hết hiệu lực")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Làm mới Token (Refresh)",
            description = "Sử dụng Refresh Token còn hiệu lực để lấy Access Token mới mà không cần đăng nhập lại."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cấp mới token thành công"),
            @ApiResponse(responseCode = "401", description = "Refresh Token không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return new ResponseEntity<>(authService.refresh(request), HttpStatus.OK);
    }

    @Operation(
            summary = "Đổi mật khẩu (Reset Password)",
            description = "Cho phép đổi mật khẩu bằng cách xác nhận username và mật khẩu cũ."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc mật khẩu mới không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.changePassword(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
