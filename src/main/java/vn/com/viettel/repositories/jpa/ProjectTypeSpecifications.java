package vn.com.viettel.repositories.jpa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.ProjectTypeSearchRequestDto;
import vn.com.viettel.entities.ProjectType;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class ProjectTypeSpecifications {

    private ProjectTypeSpecifications() {
    }

    public static Specification<ProjectType> buildSpecification(ProjectTypeSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter soft-delete
            predicates.add(cb.equal(root.get("isDeleted"), "N"));

            if (request.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtFrom()));
            }
            if (request.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtTo()));
            }

            if (StringUtils.isNotBlank(request.getProjectTypeName())) {
                String keyword = StringUtils.upperCase(StringUtils.trim(request.getProjectTypeName()));
                // LIKE %keyword%
                predicates.add(cb.like(cb.upper(cb.trim(root.get("projectTypeName"))), "%" + keyword + "%"));
            }

            // isActive default 'Y'
            String isActive = StringUtils.defaultIfBlank(request.getIsActive(), "Y");
            predicates.add(cb.equal(root.get("isActive"), isActive));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
