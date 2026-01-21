package vn.com.viettel.services.auth;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.com.viettel.dto.auth.*;
import vn.com.viettel.entities.LoginEvent;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.utils.ErrorApp;
import vn.com.viettel.utils.Translator;
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
    private final Translator translator;

    public LoginResponse login(LoginRequest request) throws Exception {
        String username = request.getUsername();
        try {
            KeycloakTokenResponse response = keycloakService.login(request.getUsername(), request.getPassword());

            if (response.getStatusCode() == 200) {
                LoginResponse successResponse = handleLoginSuccess(response, username);
                publishLoginLog(username, true, "Đăng nhập thành công");
                return successResponse;
            } else {
                return handleAuthenticationFailure(response, username);
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

        // 2. Kiểm tra trạng thái người dùng
        validateUserActive(user);

        // 3. Reset trạng thái thông báo khóa khi đăng nhập thành công
        if (Boolean.TRUE.equals(user.getIsLockedTemporarily())) {
            user.setIsLockedTemporarily(false);
            userRepository.save(user);
        }

        // 4. Chính sách mật khẩu 90 ngày cho người dùng Type 1
        policyService.checkPasswordPolicy(user, username);

        log.info("Người dùng {} đăng nhập thành công", username);

        return buildLoginResponse(response, user.getType());
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
                String username = SignedJWT.parse(response.getAccessToken()).getJWTClaimsSet().getStringClaim("preferred_username");

                // 3. Kiểm tra User trong DB Oracle
                SysUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                        .orElseThrow(() -> new CustomException(ErrorApp.BAD_NOT_FOUND.getCode(),
                            translator.getMessage("auth.user.not-found-or-deleted")));

                // Kiểm tra trạng thái hoạt động của User
                try {
                    validateUserActive(user);
                } catch (CustomException e) {
                    keycloakService.logout(response.getRefreshToken());
                    throw e;
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
                throw new CustomException(ErrorApp.UNAUTHORIZED.getCode(), translator.getMessage("auth.session.expired"));
            }
        } catch (Exception e) {
            log.error("Lỗi khi refresh token: {}", e.getMessage());
            if (e instanceof CustomException) throw (CustomException) e;
            throw new RuntimeException(translator.getMessage("auth.system.unavailable"));
        }
    }

    public void changePassword(ResetPasswordRequest request) {
        String username = request.getUsername();

        try {
            // 1. Xác thực người dùng tồn tại trên cả keycloak và database
            String userId = keycloakService.getUserIdByUsername(username);

            SysUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                    .orElse(null);

            if (userId == null || user == null) {
                throw new CustomException(ErrorApp.BAD_NOT_FOUND.getCode(), translator.getMessage("auth.user.not-found-or-deleted"));
            }

            // 2. Kiểm tra trạng thái User trước khi cho đổi pass
            validateUserActive(user);

            // 3. Xác thực mật khẩu cũ bằng cách thử "Login" ngầm sang Keycloak
            KeycloakTokenResponse verifyRes = keycloakService.login(username, request.getOldPassword());
            if (!isAuthenticationSuccess(verifyRes)) {
                handleAuthenticationFailure(verifyRes, username);
            }

            // 4. Thực hiện đổi mật khẩu mới bằng quyền Admin
            keycloakService.resetPassword(userId, request.getNewPassword());
    
            // 5. Cập nhật ngày đổi mật khẩu trong DB Oracle để reset chu kỳ 90 ngày
            user.setLastPasswordChange(LocalDateTime.now());
            userRepository.save(user);
    
            log.info("Người dùng {} đã reset mật khẩu thành công bằng mật khẩu cũ", username);
            publishLoginLog(username, true, translator.getMessage("auth.password.reset.success"));
    
        } catch (IOException e) {
            log.error("Lỗi khi kết nối Keycloak để reset mật khẩu: {}", e.getMessage());
            throw new RuntimeException(translator.getMessage("auth.system.unavailable"));
        }
    }

    private void validateUserActive(SysUser user) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new CustomException(ErrorApp.FORBIDDEN.getCode(), translator.getMessage("auth.user.inactive-or-locked"));
        }
    }

    private boolean isAuthenticationSuccess(KeycloakTokenResponse response) {
        String description = response.getErrorDescription() != null ? response.getErrorDescription() : "";
        return response.getStatusCode() == 200 ||
               (response.getStatusCode() == 400 && "Account is not fully set up".equalsIgnoreCase(description));
    }

    private LoginResponse handleAuthenticationFailure(KeycloakTokenResponse response, String username) {
        String description = response.getErrorDescription() != null ? response.getErrorDescription().toLowerCase() : "";

        // Lỗi User bị Admin disable trên Keycloak
        if (response.getStatusCode() == 400 && description.contains("disabled")) {
            throw new CustomException(ErrorApp.FORBIDDEN.getCode(), translator.getMessage("auth.user.inactive-or-locked"));
        }

        // Lỗi sai thông tin hoặc bị brute-force
        if (response.getStatusCode() == 401 || response.getStatusCode() == 400) {
            userRepository.findByUsernameAndIsDeletedFalse(username).ifPresent(user -> {
                String userId = keycloakService.getUserIdByUsername(username);
                if (userId != null) policyService.handleBruteForceLogic(user, userId);
            });
        }

        log.error("Lỗi xác thực cho người dùng {}: {}", username, description);
        throw new CustomException(ErrorApp.UNAUTHORIZED.getCode(), translator.getMessage("auth.login.invalid-credential"));
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