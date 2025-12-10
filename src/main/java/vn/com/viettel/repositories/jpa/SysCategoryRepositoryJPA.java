package vn.com.viettel.repositories.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.entities.SysCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysCategoryRepositoryJPA extends JpaRepository<SysCategory, Long> {

    @Query("  SELECT sc FROM SysCategory sc WHERE 1 = 1" +
            " AND sc.type = :#{#dto.type}" +
            " AND (:#{#dto.code} is null or LOWER(sc.code) like %:#{#dto.code}% ESCAPE '!')" +
            " AND (:#{#dto.name} is null or LOWER(sc.name) like %:#{#dto.name}% ESCAPE '!')" +
            " AND (:#{#dto.status} is null or sc.status = :#{#dto.status})" +
            " AND (:#{#dto.modifiedBy} is null or sc.modifiedBy = :#{#dto.modifiedBy}) " +
            " ORDER BY sc.id DESC")
    Page<SysCategory> getSysCategories(@Param("dto") SysCategoryDTO dto, Pageable pageable);

    @Query("  SELECT sc FROM SysCategory sc WHERE 1 = 1" +
            " AND sc.type = :#{#dto.type}" +
            " AND (:#{#dto.code} is null or LOWER(sc.code) like %:#{#dto.code}% ESCAPE '!')" +
            " AND (:#{#dto.name} is null or LOWER(sc.name) like %:#{#dto.name}% ESCAPE '!')" +
            " AND (:#{#dto.status} is null or sc.status = :#{#dto.status})" +
            " AND (:#{#dto.modifiedBy} is null or sc.modifiedBy = :#{#dto.modifiedBy}) " +
            " ORDER BY sc.id DESC")
    List<SysCategory> getSysCategoriesForExport(@Param("dto") SysCategoryDTO dto);

    Optional<SysCategory> findByCodeAndType(String code, Short type);

    @Query("SELECT new vn.com.viettel.dto.SysCategoryDTO(sc.id, sc.code, sc.name, sc.type) FROM SysCategory sc WHERE sc.status = '1' ORDER BY sc.type, sc.name")
    List<SysCategoryDTO> getAllSysCategories();
}
