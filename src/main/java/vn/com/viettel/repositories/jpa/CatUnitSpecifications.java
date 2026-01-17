package vn.com.viettel.repositories.jpa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.entities.CatUnit;

import jakarta.persistence.criteria.Predicate;
import vn.com.viettel.dto.CatUnitSearchRequestDto;

import java.util.ArrayList;
import java.util.List;

public final class CatUnitSpecifications {

    private CatUnitSpecifications() {
    }

    public static Specification<CatUnit> buildSpecification(CatUnitSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always soft-delete filter
            predicates.add(cb.equal(root.get("isDeleted"), "N"));

            if (request != null) {
                if (request.getCreatedAtFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtFrom()));
                }
                if (request.getCreatedAtTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtTo()));
                }

                if (StringUtils.isNotBlank(request.getUnitName())) {
                    String value = StringUtils.upperCase(StringUtils.trim(request.getUnitName()));
                    predicates.add(cb.like(cb.upper(cb.trim(root.get("unitName"))), "%" + value + "%"));
                }

                if (StringUtils.isNotBlank(request.getUnitType())) {
                    String value = StringUtils.upperCase(StringUtils.trim(request.getUnitType()));
                    predicates.add(cb.like(cb.upper(cb.trim(root.get("unitType"))), "%" + value + "%"));
                }

                String isActive = StringUtils.defaultIfBlank(request.getIsActive(), "Y");
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
