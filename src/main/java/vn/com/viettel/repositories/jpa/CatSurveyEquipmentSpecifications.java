package vn.com.viettel.repositories.jpa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.entities.CatSurveyEquipment;

import jakarta.persistence.criteria.Predicate;

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
            String isActive = StringUtils.defaultIfBlank(request.getIsActive(), "Y").trim().toUpperCase();
            predicates.add(cb.equal(root.get("isActive"), isActive));

            if (request.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtFrom()));
            }
            if (request.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtTo()));
            }

            if (StringUtils.isNotBlank(request.getEquipmentCode())) {
                String keyword = normalizeLike(request.getEquipmentCode());
                predicates.add(cb.like(cb.upper(cb.trim(root.get("equipmentCode"))), keyword));
            }

            if (StringUtils.isNotBlank(request.getEquipmentName())) {
                String keyword = normalizeLike(request.getEquipmentName());
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
