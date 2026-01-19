package vn.com.viettel.repositories.jpa;

import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.entities.CatSurveyEquipment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class CatSurveyEquipmentSpecifications {

    private CatSurveyEquipmentSpecifications() {
    }

    public static Specification<CatSurveyEquipment> buildSpecification(CatSurveyEquipmentSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ALWAYS: IS_DELETED = 'N'
            predicates.add(cb.equal(root.get("isDeleted"), "N"));

            // isActive default 'Y'
            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            LocalDate createdFrom = request.getCreatedFrom();
            LocalDate createdTo = request.getCreatedTo();
            if (createdFrom != null || createdTo != null) {
                if (createdFrom != null) {
                    LocalDateTime fromDateTime = createdFrom.atStartOfDay();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
                }
                if (createdTo != null) {
                    LocalDateTime toDateTime = createdTo.atTime(LocalTime.MAX);
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
                }
            }

            if (StringUtils.isNotBlank(request.getKeyword())) {
                String keyword = normalizeLike(request.getKeyword());
                predicates.add(cb.like(cb.upper(cb.trim(root.get("equipmentName"))), keyword));
            }

            if (request.getManageUnitId() != null) {
                predicates.add(cb.equal(root.get("manageUnitId"), request.getManageUnitId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalizeLike(String input) {
        String val = StringUtils.defaultString(input).trim().toUpperCase();
        return "%" + val + "%";
    }
}
