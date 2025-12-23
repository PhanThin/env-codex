package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.SysOrg;

import java.util.List;
import java.util.Optional;

public interface SysOrgRepository extends JpaRepository<SysOrg, Long> {
    List<SysOrg> findAllByIsDeletedFalse();
    Optional<SysOrg> findByIdAndIsDeletedFalse(Long orgId);

    boolean existsByOrgCodeAndIsDeletedFalse(String orgCode);

    Optional<SysOrg> findByOrgCodeAndIsDeletedFalse(String orgCode);

    /**
     * Case-insensitive check for Oracle (using UPPER).
     * Keeps original required methods; this is an extra helper for unique orgCode.
     */
    @Query("select case when count(o) > 0 then true else false end " +
            "from SysOrg o " +
            "where o.isDeleted = 'N' and upper(o.orgCode) = upper(:orgCode)")
    boolean existsByOrgCodeIgnoreCaseAndIsDeletedFalse(@Param("orgCode") String orgCode);

    @Query("select o from SysOrg o " +
            "where o.isDeleted = 'N' and upper(o.orgCode) = upper(:orgCode)")
    Optional<SysOrg> findByOrgCodeIgnoreCaseAndIsDeletedFalse(@Param("orgCode") String orgCode);
}
