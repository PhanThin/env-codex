package vn.com.viettel.repositories.jpa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.CatManufacturerSearchRequestDto;
import vn.com.viettel.entities.CatManufacturer;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class CatManufacturerSpecifications {

    private CatManufacturerSpecifications() {}

    public static Specification<CatManufacturer> buildSpecification(CatManufacturerSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ALWAYS filter not deleted
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (request.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtFrom()));
            }
            if (request.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtTo()));
            }

            if (StringUtils.isNotBlank(request.getManufacturerCode())) {
                String v = "%" + request.getManufacturerCode().trim().toUpperCase() + "%";
                predicates.add(cb.like(cb.upper(cb.trim(root.get("manufacturerCode"))), v));
            }
            if (StringUtils.isNotBlank(request.getManufacturerName())) {
                String v = "%" + request.getManufacturerName().trim().toUpperCase() + "%";
                predicates.add(cb.like(cb.upper(cb.trim(root.get("manufacturerName"))), v));
            }
            if (StringUtils.isNotBlank(request.getCountry())) {
                String v = "%" + request.getCountry().trim().toUpperCase() + "%";
                predicates.add(cb.like(cb.upper(cb.trim(root.get("country"))), v));
            }

            Boolean isActive = request.getIsActive();
            if (isActive == null) {
                isActive = Boolean.TRUE;
            }
            predicates.add(cb.equal(root.get("isActive"), isActive));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
