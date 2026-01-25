package vn.com.viettel.controllers.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.viettel.services.auth.UserMigrationService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
public class AuthAdminController {

    private final UserMigrationService migrationService;

    @PostMapping("/migrate-ids")
    public ResponseEntity<Map<String, Object>> triggerMigration() {
        Map<String, Object> report = migrationService.migrateExistingUsers();
        return ResponseEntity.ok(report);
    }
}
