package vn.com.viettel.repositories.jpa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.CategoryWorkItemSearchRequest;
import vn.com.viettel.entities.CategoryWorkItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class CategoryWorkItemSpecifications {
    private CategoryWorkItemSpecifications() {
    }

    public static Specification<CategoryWorkItem> buildSpecification(CategoryWorkItemSearchRequest request) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            // Luôn chỉ lấy bản ghi chưa xóa
            predicate = cb.and(predicate,
                    cb.isFalse(root.get("isDeleted"))
            );

            // keyword: id / code / name (LIKE)
            if (StringUtils.isNotBlank(request.getKeyword())) {
                String kw = "%" + request.getKeyword().trim().toLowerCase() + "%";

                var idPredicate = cb.disjunction();
                if (StringUtils.isNumeric(request.getKeyword().trim())) {
                    Long idVal = Long.parseLong(request.getKeyword().trim());
                    idPredicate = cb.equal(root.get("id"), idVal);
                }

                var codePredicate = cb.like(
                        cb.lower(root.get("categoryWorkItemCode")),
                        kw
                );
                var namePredicate = cb.like(
                        cb.lower(root.get("categoryWorkItemName")),
                        kw
                );

                predicate = cb.and(predicate,
                        cb.or(idPredicate, codePredicate, namePredicate)
                );
            }

            if (request.getProjectTypeId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("projectTypeId"), request.getProjectTypeId())
                );
            }

            if (request.getPhaseId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("projectPhaseId"), request.getPhaseId())
                );
            }

            if (request.getProjectItemId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("projectItemId"), request.getProjectItemId())
                );
            }

            if (request.getUnitId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("unitId"), request.getUnitId())
                );
            }

            if (request.getIsActive() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("isActive"), request.getIsActive())
                );
            }

            LocalDate createdFrom = request.getCreatedFrom();
            LocalDate createdTo = request.getCreatedTo();

            if (createdFrom != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                createdFrom.atStartOfDay()
                        )
                );
            }

            if (createdTo != null) {
                // đến cuối ngày
                LocalDateTime endOfDay = createdTo.atTime(23, 59, 59);
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                endOfDay
                        )
                );
            }

            return predicate;
        };
    }
}
