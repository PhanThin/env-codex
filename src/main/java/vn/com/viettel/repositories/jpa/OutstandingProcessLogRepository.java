package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingProcessLog;

public interface OutstandingProcessLogRepository extends JpaRepository<OutstandingProcessLog, Long> {
}
