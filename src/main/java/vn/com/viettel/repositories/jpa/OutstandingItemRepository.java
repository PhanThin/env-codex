package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingItem;

public interface OutstandingItemRepository extends JpaRepository<OutstandingItem, Long> {
}
