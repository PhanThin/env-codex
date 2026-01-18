package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.CatScheduleAdjReason;

import java.util.Optional;

public interface CatScheduleAdjReasonRepository extends JpaRepository<CatScheduleAdjReason, Long>, JpaSpecificationExecutor<CatScheduleAdjReason> {

    Optional<CatScheduleAdjReason> findByReasonIdAndIsDeletedFalse(Long reasonId);

    boolean existsByReasonCodeAndIsDeletedFalse(String reasonCode);

    @Query("select count(r.reasonId) > 0 from CatScheduleAdjReason r " +
            "where r.isDeleted = false and upper(trim(r.reasonName)) = upper(trim(:reasonName))")
    boolean existsByReasonNameIgnoreCaseAndTrimAndIsDeletedFalse(@Param("reasonName") String reasonName);

    @Query("select count(r.reasonId) > 0 from CatScheduleAdjReason r " +
            "where r.isDeleted = false and r.reasonCode = :reasonCode and r.reasonId <> :reasonId")
    boolean existsByReasonCodeAndIsDeletedFalseAndReasonIdNot(@Param("reasonCode") String reasonCode, @Param("reasonId") Long reasonId);

    @Query("select count(r.reasonId) > 0 from CatScheduleAdjReason r " +
            "where r.isDeleted = false and upper(trim(r.reasonName)) = upper(trim(:reasonName)) and r.reasonId <> :reasonId")
    boolean existsByReasonNameIgnoreCaseAndTrimAndIsDeletedFalseAndReasonIdNot(@Param("reasonName") String reasonName, @Param("reasonId") Long reasonId);
}
