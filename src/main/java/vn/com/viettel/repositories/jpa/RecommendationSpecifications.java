package vn.com.viettel.repositories.jpa;

import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.entities.Recommendation;
import vn.com.viettel.entities.RecommendationWorkItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class RecommendationSpecifications {

    private RecommendationSpecifications() {
    }

    public static Specification<Recommendation> buildSpecification(RecommendationSearchRequestDto req) {
        return (Root<Recommendation> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            // Không lấy bản ghi đã xóa
            predicate = cb.and(predicate, cb.isFalse(root.get("isDeleted")));

            // Từ khóa - tìm theo tên kiến nghị (LIKE, case-insensitive)
            if (StringUtils.isNotBlank(req.getKeyword())) {
                String pattern = "%" + req.getKeyword().trim().toLowerCase() + "%";
                predicate = cb.and(
                        predicate,
                        cb.like(cb.lower(root.get("recommendationTitle")), pattern)
                );
            }

            // Dự án
            if (req.getProjectId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("projectId"), req.getProjectId()));
            }

            // Hạng mục
            if (req.getItemId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("itemId"), req.getItemId()));
            }

            // Loại kiến nghị
            if (req.getRecommendationTypeId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("recommendationTypeId"), req.getRecommendationTypeId()));
            }

            // Mức độ quan trọng (bỏ qua nếu null/blank/ALL)
            if (StringUtils.isNotBlank(req.getPriority())
                    && !"ALL".equalsIgnoreCase(req.getPriority().trim())) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("priority"), req.getPriority().trim()));
            }

            // Trạng thái xử lý (bỏ qua nếu null/blank/ALL)
            if (StringUtils.isNotBlank(req.getStatus())
                    && !"ALL".equalsIgnoreCase(req.getStatus().trim())) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("status"), req.getStatus().trim()));
            }

            // Đơn vị (createdOrgId)
            if (req.getOrgId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("createdOrgId"), req.getOrgId()));
            }

            // Người tạo
            if (req.getCreatedById() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("createdById"), req.getCreatedById()));
            }

            // Người xử lý (closedById)
            if (req.getClosedById() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("closedById"), req.getClosedById()));
            }

            // Thời gian tạo (createdAt) trong khoảng ngày
            LocalDate createdFrom = req.getCreatedFrom();
            LocalDate createdTo = req.getCreatedTo();
            if (createdFrom != null || createdTo != null) {
                if (createdFrom != null) {
                    LocalDateTime fromDateTime = createdFrom.atStartOfDay();
                    predicate = cb.and(predicate,
                            cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
                }
                if (createdTo != null) {
                    LocalDateTime toDateTime = createdTo.atTime(LocalTime.MAX);
                    predicate = cb.and(predicate,
                            cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
                }
            }

            // Hạn xử lý (deadline) trong khoảng
            LocalDate dlFrom = req.getDeadlineFrom();
            LocalDate dlTo = req.getDeadlineTo();
            if (dlFrom != null || dlTo != null) {
                if (dlFrom != null) {
                    predicate = cb.and(predicate,
                            cb.greaterThanOrEqualTo(root.get("deadline"), dlFrom));
                }
                if (dlTo != null) {
                    predicate = cb.and(predicate,
                            cb.lessThanOrEqualTo(root.get("deadline"), dlTo));
                }
            }

            // Công việc: tồn tại dòng trong RECOMMENDATION_WORK_ITEM với workItemId tương ứng
            if (req.getWorkItemId() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<RecommendationWorkItem> rwiRoot = subquery.from(RecommendationWorkItem.class);
                subquery.select(rwiRoot.get("recommendationId"))
                        .where(
                                cb.equal(rwiRoot.get("recommendationId"), root.get("id")),
                                cb.equal(rwiRoot.get("workItemId"), req.getWorkItemId()),
                                cb.isFalse(rwiRoot.get("isDeleted"))
                        );
                predicate = cb.and(predicate, cb.exists(subquery));
            }

            return predicate;
        };
    }

    public static Specification<Recommendation> withCustomSort(String sortBy, Sort.Direction direction) {
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
                        .when("DONE", 2)
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
            }

            return cb.conjunction();
        };
    }
}
