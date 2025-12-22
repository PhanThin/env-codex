package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingItem;

import java.util.Optional;

public interface OutstandingItemRepository extends JpaRepository<OutstandingItem, Long> {
    Optional<OutstandingItem> findByIdAndIsDeletedFalse(Long id);

    Optional<OutstandingItem> findByOutstandingTitleAndIsDeletedFalse(String title);
}
