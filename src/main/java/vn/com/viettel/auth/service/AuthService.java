package vn.com.viettel.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.com.viettel.auth.constant.AuthErrorCode;
import vn.com.viettel.auth.dto.*;
import vn.com.viettel.auth.entity.LoginEvent;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.utils.exceptions.CustomException;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final SysUserRepository userRepository;
    private final AuthPolicyService policyService;
    private final UserSyncService userSyncService;
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest request;

    public LoginResponse login(LoginRequest request) throws Exception {
        String username = request.getUsername();
        try {
            // KeycloakService giờ trả về Object đã parse sẵn
            KeycloakTokenResponse response = keycloakService.login(request.getUsername(), request.getPassword());

            if (response.getStatusCode() == 200) {
                LoginResponse successResponse = handleLoginSuccess(response, username);
                publishLoginLog(username, true, "Đăng nhập thành công");
                return successResponse;
            } else {
                return handleLoginError(response, request.getUsername());
            }
        } catch (Exception e) {
            String errorMsg = (e instanceof CustomException) ? e.getMessage() : "Lỗi hệ thống xác thực";
            publishLoginLog(username, false, errorMsg);
            throw e;
        }
    }

    protected LoginResponse handleLoginSuccess(KeycloakTokenResponse response, String username) throws Exception {
        String accessToken = response.getAccessToken();

        // 1. Xác định User từ DB hoặc đồng bộ mới
        SysUser user = userSyncService.syncUserFromPayload(username, accessToken);

        // Reset trạng thái thông báo khóa khi đăng nhập thành công
        if (Boolean.TRUE.equals(user.getIsLockedTemporarily())) {
            user.setIsLockedTemporarily(false);
            userRepository.save(user);
        }

        // "Hack" to pass when api reset password not done
        user.setLastPasswordChange(LocalDateTime.now().minusDays(50));

        // 2. Kiểm tra trạng thái tài khoản
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new CustomException(AuthErrorCode.USER_LOCKED.getCode(), "Tài khoản đã bị khóa hoặc không còn hiệu lực. Vui lòng liên hệ quản trị hệ thống");
        }

        // 3. Chính sách mật khẩu 90 ngày cho tài khoản Type 1
        policyService.checkPasswordPolicy(user, username);

        log.info("Người dùng {} đăng nhập thành công", username);
        return buildLoginResponse(response, user.getType());
    }

    private LoginResponse handleLoginError(KeycloakTokenResponse response, String username) {
        String description = response.getErrorDescription() != null ? response.getErrorDescription().toLowerCase() : "";

        // 1. Trường hợp Tài khoản bị Admin vô hiệu hóa (is_active = false trong Keycloak)
        if (response.getStatusCode() == 400 && description.contains("disabled")) {
            throw new CustomException(AuthErrorCode.USER_LOCKED.getCode(), "Tài khoản đã bị khóa hoặc không còn hiệu lực. Vui lòng liên hệ quản trị hệ thống");
        }

        // Xử lý thông báo lỗi Brute-force
        if (response.getStatusCode() == 401 || response.getStatusCode() == 400) {
            SysUser user = userRepository.findByUsernameAndIsDeletedFalse(username).orElse(null);
            if (user != null) {
                String userId = keycloakService.getUserIdByUsername(username);

                if (userId != null) {
                    policyService.handleBruteForceLogic(user, userId);
                }
            }
        }

        throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS.getCode(), "Tên đăng nhập hoặc mật khẩu không đúng");
    }

    private void publishLoginLog(String username, boolean success, String message) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        eventPublisher.publishEvent(new LoginEvent(username, success, message, ip, userAgent));
    }

    public void logout(LogoutRequest request) {
        try {
            KeycloakTokenResponse response = keycloakService.logout(request.getRefreshToken());
            if (response.getStatusCode() != 204 && response.getStatusCode() != 200) {
                log.warn("Keycloak logout trả về status: {}", response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("Lỗi khi thực hiện logout: {}", e.getMessage());
        }
    }

    /**
     * Logic Reset Mật khẩu
     */
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Kiểm tra người dùng có tồn tại trong hệ thống nội bộ không
        SysUser user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND.getCode(), "Người dùng không tồn tại trên hệ thống"));

        // 2. Lấy userId từ Keycloak
        String userId = keycloakService.getUserIdByUsername(request.getUsername());
        if (userId == null) {
            throw new CustomException(AuthErrorCode.USER_NOT_FOUND.getCode(), "Không tìm thấy người dùng tương ứng trên Keycloak");
        }

        try {
            // 3. Thực hiện reset mật khẩu trên Keycloak
            keycloakService.resetPassword(userId, request.getNewPassword());

            // 4. Mở khóa tài khoản trong DB nếu đang bị khóa tạm thời (nếu cần)
            if (Boolean.TRUE.equals(user.getIsLockedTemporarily())) {
                user.setIsLockedTemporarily(false);
                userRepository.save(user);
                keycloakService.clearBruteForce(userId); // Xóa vết Brute-force trên Keycloak
            }

            log.info("Mật khẩu của người dùng {} đã được cập nhật thành công", request.getUsername());
        } catch (Exception e) {
            log.error("Lỗi khi reset mật khẩu cho {}: {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Cập nhật mật khẩu thất bại. Vui lòng thử lại sau");
        }
    }

    /**
     * Xây dựng object phản hồi đăng nhập từ dữ liệu Keycloak và thông tin User trong DB
     */
    private LoginResponse buildLoginResponse(KeycloakTokenResponse response, Integer userType) {
        return LoginResponse.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiresIn(response.getExpiresIn())
                .userType(userType)
                .requirePasswordChange(false)
                .message("Đăng nhập thành công")
                .build();
    }
}