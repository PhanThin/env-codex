package vn.com.viettel.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.com.viettel.auth.entity.LoginEvent;
import vn.com.viettel.auth.repositories.SysLoginLogRepository;
import vn.com.viettel.entities.SysLoginLog;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginEventListener {
    private final SysLoginLogRepository logRepository;

    @Async
    @EventListener
    public void onLoginEvent(LoginEvent event) {
        try {
            SysLoginLog loginLog = SysLoginLog.builder()
                    .username(event.getUsername())
                    .isSuccess(event.isSuccess())
                    .errorMessage(event.getMessage())
                    .ipAddress(event.getIp())
                    .userAgent(event.getUserAgent())
                    .loginTime(LocalDateTime.now())
                    .build();
            logRepository.save(loginLog);
        } catch (Exception e) {
            log.error("Không thể ghi log đăng nhập cho user {}: {}", event.getUsername(), e.getMessage());
        }
    }
}
