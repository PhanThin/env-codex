package vn.com.viettel.repositories.jpa;

import jakarta.persistence.criteria.Expression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.entities.OutstandingItem;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public final class OutstandingItemSpecifications {

    private OutstandingItemSpecifications() {
        // utility class
    }

    public static Specification<OutstandingItem> fromRecommendationSearch(RecommendationSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Không lấy bản ghi bị xóa
            predicates.add(
                    cb.or(
                            cb.isNull(root.get("isDeleted")),
                            cb.isFalse(root.get("isDeleted"))
                    )
            );

            if (request == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // keyword -> outstandingTitle (LIKE, ignore case)
            if (StringUtils.isNotBlank(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(
                        cb.like(cb.lower(root.get("outstandingTitle")), pattern)
                );
            }

            // projectId
            if (request.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), request.getProjectId()));
            }

            // itemId
            if (request.getItemId() != null) {
                predicates.add(cb.equal(root.get("itemId"), request.getItemId()));
            }

            // workItemId
            if (request.getWorkItemId() != null) {
                predicates.add(cb.equal(root.get("workItemId"), request.getWorkItemId()));
            }

            // priority
            if (StringUtils.isNotBlank(request.getPriority())) {
                predicates.add(cb.equal(root.get("priority"), request.getPriority()));
            }

            // status
            if (StringUtils.isNotBlank(request.getStatus())) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // orgId -> createdOrgId
            if (request.getOrgId() != null) {
                predicates.add(cb.equal(root.get("createdOrgId"), request.getOrgId()));
            }

            // createdById -> createdBy
            if (request.getCreatedById() != null) {
                predicates.add(cb.equal(root.get("createdBy"), request.getCreatedById()));
            }

            // createdFrom / createdTo -> createdAt
            if (request.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.getCreatedFrom().atStartOfDay()
                ));
            }
            if (request.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        request.getCreatedTo().atTime(23, 59, 59)
                ));
            }

            // deadlineFrom / deadlineTo -> deadline
            if (request.getDeadlineFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("deadline"),
                        request.getDeadlineFrom()
                ));
            }
            if (request.getDeadlineTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("deadline"),
                        request.getDeadlineTo()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<OutstandingItem> withCustomSort(String sortBy, Sort.Direction direction) {
        return (root, query, cb) -> {
            // Tránh ảnh hưởng đến count query
            Class<?> resultType = query.getResultType();
            if (resultType == Long.class || resultType == long.class) {
                return cb.conjunction();
            }

            // Nếu không sort theo các field đặc biệt thì bỏ qua
            if (!"status".equalsIgnoreCase(sortBy) && !"priority".equalsIgnoreCase(sortBy)) {
                return cb.conjunction();
            }

            boolean asc = direction == Sort.Direction.ASC;

            if ("status".equalsIgnoreCase(sortBy)) {
                // Thứ tự ví dụ: NEW -> IN_PROGRESS -> DONE -> CLOSED
                Expression<Object> statusOrder = cb.selectCase(root.get("status"))
                        .when("NEW", 1)
                        .when("IN_PROGRESS", 2)
                        .when("DONE", 3)
                        .when("CLOSED", 4)
                        .otherwise(99);

                query.orderBy(asc ? cb.asc(statusOrder) : cb.desc(statusOrder));
            } else if ("priority".equalsIgnoreCase(sortBy)) {
                // Thứ tự ví dụ: HIGH_PRIORITY -> PRIORITY -> LOW_PRIORITY
                Expression<Object> priorityOrder = cb.selectCase(root.get("priority"))
                        .when("HIGH_PRIORITY", 1)
                        .when("PRIORITY", 2)
                        .when("LOW_PRIORITY", 3)
                        .otherwise(99);

                query.orderBy(asc ? cb.asc(priorityOrder) : cb.desc(priorityOrder));
            } else if ("acceptanceType".equalsIgnoreCase(sortBy)) {
                Expression<Object> acceptanceTypeOrder = cb.selectCase(root.get("acceptanceType"))
                        .when("WORK_ACCEPTANCE", 1)
                        .when("ITEM_COMPLETION_ACCEPTANCE", 2)
                        .when("SURVEY_COMPLETION_ACCEPTANCE", 2)
                        .otherwise(99);

                query.orderBy(asc ? cb.asc(acceptanceTypeOrder) : cb.desc(acceptanceTypeOrder));
            }

            return cb.conjunction();
        };
    }
}
