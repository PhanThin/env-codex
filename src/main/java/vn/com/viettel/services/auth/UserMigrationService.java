package vn.com.viettel.services.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.SysUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserMigrationService {

    private final SysUserRepository userRepository;
    private final KeycloakService keycloakService;

    @Transactional
    public Map<String, Object> migrateExistingUsers() {
        log.info("Bắt đầu tiến trình Migration ID từ Keycloak...");

        List<SysUser> pendingUsers = userRepository.findAllByKeycloakIdIsNullAndIsDeletedFalse();
        int total = pendingUsers.size();
        int success = 0;
        int notFound = 0;

        for (SysUser user : pendingUsers) {
            try {
                // Gọi sang Keycloak tìm ID theo username
                String keycloakId = keycloakService.getUserIdByUsername(user.getUsername());

                if (keycloakId != null) {
                    user.setKeycloakId(keycloakId);
                    userRepository.save(user);
                    success++;
                } else {
                    notFound++;
                    log.warn("User [{}] không tồn tại trên Keycloak - Bỏ qua.", user.getUsername());
                }
            } catch (Exception e) {
                log.error("Lỗi khi migrate user [{}]: {}", user.getUsername(), e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total_processed", total);
        result.put("success_count", success);
        result.put("not_found_on_keycloak", notFound);

        log.info("Migration hoàn tất: {}", result);
        return result;
    }
}
