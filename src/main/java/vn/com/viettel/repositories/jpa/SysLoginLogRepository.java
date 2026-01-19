package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.SysLoginLog;

public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long> {

}