package vn.com.viettel.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.viettel.config.auth.AuthProperties;
import vn.com.viettel.constant.UserType;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.utils.ErrorApp;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthPolicyService {

    private final KeycloakService keycloakService;
    private final AuthProperties authProperties;
    private final SysUserRepository sysUserRepository;
    private final Translator translator;

    public void checkPasswordPolicy(SysUser user, String username) {
        if (UserType.INTERNAL.getValue().equals(user.getType())) {
            boolean isFirstLogin = (user.getLastPasswordChange() == null);
            boolean isExpired = (user.getLastPasswordChange() != null &&
                    user.getLastPasswordChange().plusDays(90).isBefore(LocalDateTime.now()));

            String userId = keycloakService.getUserIdByUsername(username);

            if (isFirstLogin) {
                if (userId != null) {
                    keycloakService.forcePasswordUpdate(userId);
                }
                throw new CustomException(ErrorApp.FORBIDDEN.getCode(), translator.getMessage("auth.password.change.required"));
            }

            if (isExpired) {
                if (userId != null) {
                    keycloakService.forcePasswordUpdate(userId);
                }
                throw new CustomException(ErrorApp.FORBIDDEN.getCode(), translator.getMessage("auth.password.expired"));
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
                        throw new CustomException(ErrorApp.FORBIDDEN.getCode(),
                                translator.getMessage("auth.login.bruteforce.locked", authProperties.getBruteForce().getLockDurationMinutes()));
                    }
                }
                throw new CustomException(ErrorApp.FORBIDDEN.getCode(), translator.getMessage("auth.login.temporarily-locked"));
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