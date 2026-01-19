package vn.com.viettel.repositories.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.CatManufacturer;
import vn.com.viettel.entities.CatSurveyEquipment;

import java.util.List;
import java.util.Optional;

public interface CatSurveyEquipmentRepository extends JpaRepository<CatSurveyEquipment, Long>, JpaSpecificationExecutor<CatSurveyEquipment> {

    List<CatSurveyEquipment> findAllByIsDeletedFalse(Sort sort);

    Optional<CatSurveyEquipment> findByEquipmentIdAndIsDeleted(Long equipmentId, String isDeleted);

    List<CatSurveyEquipment> findAllByEquipmentIdInAndIsDeleted(List<Long> equipmentIds, String isDeleted);

    @Query("""
        select (count(e) > 0) from CatSurveyEquipment e
        where e.isDeleted = false and upper(trim(e.equipmentCode)) = :code
        """)
    boolean existsActiveByNormalizedEquipmentCode(@Param("code") String normalizedCode);

    @Query("select (count(e) > 0) from CatSurveyEquipment e " +
            "where e.isDeleted = false and upper(trim(e.equipmentCode)) = :code and e.equipmentId <> :id")
    boolean existsActiveByNormalizedEquipmentCodeAndNotId(@Param("code") String normalizedCode, @Param("id") Long id);

    @Query("select (count(e) > 0) from CatSurveyEquipment e " +
            "where e.isDeleted = false and upper(trim(e.equipmentName)) = :name")
    boolean existsActiveByNormalizedEquipmentName(@Param("name") String normalizedName);

    @Query("select (count(e) > 0) from CatSurveyEquipment e " +
            "where e.isDeleted = false and upper(trim(e.equipmentName)) = :name and e.equipmentId <> :id")
    boolean existsActiveByNormalizedEquipmentNameAndNotId(@Param("name") String normalizedName, @Param("id") Long id);
}
