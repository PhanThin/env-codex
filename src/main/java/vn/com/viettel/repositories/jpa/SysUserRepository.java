package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.SysUser;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);

    List<SysUser> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    List<SysUser> findAllByIsDeletedFalse();
}
