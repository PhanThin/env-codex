package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.SysOrg;

import java.util.List;

public interface SysOrgRepository extends JpaRepository<SysOrg, Long> {
    List<SysOrg> findAllByIsDeletedFalse();
}
