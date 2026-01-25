package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.SysUser;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);

    List<SysUser> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    List<SysUser> findAllByIsDeletedFalse();
    Optional<SysUser> findByIdAndIsDeletedFalse(Long userId);

    Optional<SysUser> findByKeycloakId(String keycloakId);

    List<SysUser> findAllByKeycloakIdIsNullAndIsDeletedFalse();

    boolean existsByUsernameAndIsDeletedFalse(String username);

    Optional<SysUser> findByUsernameAndIsDeletedFalse(String username);

    /**
     * Case-insensitive check for Oracle (using UPPER).
     * Keeps original required methods; this is an extra helper for unique username.
     */
    @Query("select case when count(u) > 0 then true else false end " +
            "from SysUser u " +
            "where u.isDeleted = 'N' and upper(u.username) = upper(:username)")
    boolean existsByUsernameIgnoreCaseAndIsDeletedFalse(@Param("username") String username);

    @Query("select u from SysUser u " +
            "where u.isDeleted = 'N' and upper(u.username) = upper(:username)")
    Optional<SysUser> findByUsernameIgnoreCaseAndIsDeletedFalse(@Param("username") String username);
}
