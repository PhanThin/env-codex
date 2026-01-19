package vn.com.viettel.auth.service;

import com.nimbusds.jwt.SignedJWT;
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
//        user.setLastPasswordChange(LocalDateTime.now().minusDays(50));

        // 2. Kiểm tra trạng thái người dùng
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new CustomException(AuthErrorCode.USER_LOCKED.getCode(), "Người dùng đã bị khóa hoặc không còn hiệu lực. Vui lòng liên hệ quản trị hệ thống");
        }

        // 3. Chính sách mật khẩu 90 ngày cho người dùng Type 1
        policyService.checkPasswordPolicy(user, username);

        log.info("Người dùng {} đăng nhập thành công", username);
        return buildLoginResponse(response, user.getType());
    }

    private LoginResponse handleLoginError(KeycloakTokenResponse response, String username) {
        String description = response.getErrorDescription() != null ? response.getErrorDescription().toLowerCase() : "";

        // 1. Trường hợp Người dùng bị Admin vô hiệu hóa (is_active = false trong Keycloak)
        if (response.getStatusCode() == 400 && description.contains("disabled")) {
            throw new CustomException(AuthErrorCode.USER_LOCKED.getCode(), "Người dùng đã bị khóa hoặc không còn hiệu lực. Vui lòng liên hệ quản trị hệ thống");
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

        log.info(description);
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

    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            // 1. Gọi Keycloak để lấy bộ Token mới
            KeycloakTokenResponse response = keycloakService.refreshToken(refreshToken);

            if (response.getStatusCode() == 200) {
                // 2. Trích xuất username từ Access Token mới để kiểm tra DB nội bộ
                // Sử dụng thư viện Nimbus (mặc định của Spring Security) để parse JWT không cần verify (vì Keycloak đã verify rồi)
                String username = SignedJWT.parse(response.getAccessToken()).getJWTClaimsSet().getStringClaim("preferred_username");

                // 3. Kiểm tra User trong DB Oracle
                SysUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                        .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND.getCode(),
                            "Người dùng không tồn tại hoặc đã bị xóa"));

                if (!Boolean.TRUE.equals(user.getIsActive())) {
                    // Nếu User bị khóa, chúng ta nên Logout luôn session này trên Keycloak (Tùy chọn)
                    keycloakService.logout(response.getRefreshToken());
                    throw new CustomException(AuthErrorCode.USER_LOCKED.getCode(),
                        "Người dùng đã bị khóa hoặc không còn hiệu lực. Vui lòng liên hệ quản trị hệ thống");
                }

                // 4. Ghi log làm mới token thành công (Async Event)
                publishLoginLog(username, true, "Làm mới phiên làm việc thành công");

                return LoginResponse.builder()
                        .accessToken(response.getAccessToken())
                        .refreshToken(response.getRefreshToken())
                        .expiresIn(response.getExpiresIn())
                        .userType(user.getType())
                        .message("Duy trì đăng nhập thành công")
                        .build();
            } else {
                publishLoginLog("UNKNOWN", false, "Refresh token không hợp lệ hoặc đã hết hạn");
                throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS.getCode(), "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại");
            }
        } catch (Exception e) {
            log.error("Lỗi khi refresh token: {}", e.getMessage());
            if (e instanceof CustomException) throw (CustomException) e;
            throw new RuntimeException("Hệ thống xác thực đang gặp sự cố");
        }
    }

    public void changePassword(ResetPasswordRequest request) {
        String username = request.getUsername();

        try {
            // 1. Xác thực mật khẩu cũ bằng cách thử "Login" ngầm sang Keycloak
            KeycloakTokenResponse verifyRes = keycloakService.login(username, request.getOldPassword());

            boolean isOldPasswordCorrect = false;
            if (verifyRes.getStatusCode() == 200) {
                isOldPasswordCorrect = true;
            } else if (verifyRes.getStatusCode() == 400 &&
                       "Account is not fully set up".equalsIgnoreCase(verifyRes.getErrorDescription())) {
                // ĐÂY LÀ ĐIỂM MẤU CHỐT: Keycloak xác nhận pass đúng nhưng yêu cầu action bổ sung
                log.info("Xác minh mật khẩu cũ thành công (Tài khoản đang có Required Action)");
                isOldPasswordCorrect = true;
            }

            if (!isOldPasswordCorrect) {
                throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS.getCode(), "Mật khẩu cũ không chính xác");
            }

            // 2. Tìm userId của người dùng trên Keycloak thông qua username
            String userId = keycloakService.getUserIdByUsername(username);
            if (userId == null) {
                throw new CustomException(AuthErrorCode.USER_NOT_FOUND.getCode(), "Người dùng không tồn tại hoặc đã bị xóa");
            }
    
            // 3. Thực hiện đổi mật khẩu mới bằng quyền Admin
            keycloakService.resetPassword(userId, request.getNewPassword());
    
            // 4. Cập nhật ngày đổi mật khẩu trong DB Oracle để reset chu kỳ 90 ngày
            SysUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND.getCode(), "Người dùng không tồn tại hoặc đã bị xóa"));

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new CustomException(AuthErrorCode.USER_LOCKED.getCode(),
                        "Người dùng đã bị khóa hoặc không còn hiệu lực. Vui lòng liên hệ quản trị hệ thống");
            }

            user.setLastPasswordChange(LocalDateTime.now());
            userRepository.save(user);
    
            log.info("Người dùng {} đã reset mật khẩu thành công bằng mật khẩu cũ", username);
            publishLoginLog(username, true, "Reset mật khẩu thành công");
    
        } catch (IOException e) {
            log.error("Lỗi khi kết nối Keycloak để reset mật khẩu: {}", e.getMessage());
            throw new RuntimeException("Hệ thống xác thực đang gặp sự cố");
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