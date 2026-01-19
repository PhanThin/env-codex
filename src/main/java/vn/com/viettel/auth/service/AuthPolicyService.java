package vn.com.viettel.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.viettel.auth.constant.AuthErrorCode;
import vn.com.viettel.auth.config.AuthProperties;
import vn.com.viettel.auth.constant.UserType;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthPolicyService {

    private final KeycloakService keycloakService;
    private final AuthProperties authProperties;
    private final SysUserRepository sysUserRepository;

    public void checkPasswordPolicy(SysUser user, String username) {
        if (UserType.INTERNAL.getValue().equals(user.getType())) {
            boolean isFirstLogin = (user.getLastPasswordChange() == null);
            boolean isExpired = (user.getLastPasswordChange() != null &&
                    user.getLastPasswordChange().plusDays(90).isBefore(LocalDateTime.now()));

            if (isFirstLogin) {
                String userId = keycloakService.getUserIdByUsername(username);
                if (userId != null) {
                    keycloakService.forcePasswordUpdate(userId);
                }
                throw new CustomException(AuthErrorCode.PASSWORD_EXPIRED.getCode(), "Bạn cần thay đổi mật khẩu trước khi sử dụng hệ thống.");
            }

            if (isExpired) {
                throw new CustomException(AuthErrorCode.PASSWORD_EXPIRED.getCode(), "Mật khẩu đã hết hạn." +
                        "Bạn cần thay đổi mật khẩu trước khi sử dụng hệ thống.");
            }
        }
    }

    public void handleBruteForceLogic(SysUser user, String userId) {
        if (UserType.INTERNAL.getValue().equals(user.getType())) {
            Map<String, Object> status = keycloakService.getBruteForceStatus(userId);

            boolean isLocked = Boolean.TRUE.equals(status.get("disabled"));
            Object failuresObj = status.getOrDefault("numFailures", 0);
            int numLoginFailures = Integer.parseInt(failuresObj.toString());

            if (isLocked) {
                if (!Boolean.TRUE.equals(user.getIsLockedTemporarily())) {
                    user.setIsLockedTemporarily(true);
                    sysUserRepository.save(user);
                    if (numLoginFailures >= authProperties.getBruteForce().getMaxFailures()) {
                        throw new CustomException(AuthErrorCode.BRUTE_FORCE_LOCKED.getCode(),
                                "Tài khoản đã tạm thời bị khóa do đăng nhập sai quá số lần cho phép Vui lòng thử lại sau " +
                                        authProperties.getBruteForce().getLockDurationMinutes() + " phút");
                    }
                }
                throw new CustomException(AuthErrorCode.BRUTE_FORCE_LOCKED.getCode(), "Tài khoản đang bị tạm khóa. Vui lòng thử lại sau khi hết thời gian khóa");
            }
        } else if (UserType.IMIS.getValue().equals(user.getType())) {
            keycloakService.clearBruteForce(userId);
            if (Boolean.TRUE.equals(user.getIsLockedTemporarily())) {
                user.setIsLockedTemporarily(false);
                sysUserRepository.save(user);
            }
        }
    }
}