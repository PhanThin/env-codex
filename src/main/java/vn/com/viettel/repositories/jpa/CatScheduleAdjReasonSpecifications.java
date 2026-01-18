package vn.com.viettel.repositories.jpa;

import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.com.viettel.dto.CatScheduleAdjReasonSearchRequestDto;
import vn.com.viettel.entities.CatScheduleAdjReason;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class CatScheduleAdjReasonSpecifications {

    private CatScheduleAdjReasonSpecifications() {
    }

    public static Specification<CatScheduleAdjReason> buildSpecification(CatScheduleAdjReasonSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ALWAYS: IS_DELETED = 'N'
            predicates.add(cb.isFalse(root.get("isDeleted")));

            // isActive: default Y(true)
            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            if (StringUtils.isNotBlank(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("reasonName")), pattern));
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalizeLike(String input) {
        return "%" + StringUtils.upperCase(StringUtils.trim(input)) + "%";
    }

    /**
     * Accept formats:
     * - yyyy-MM-dd'T'HH:mm:ss.SSS
     * - yyyy-MM-dd'T'HH:mm:ss
     * - yyyy-MM-dd
     */
    private static LocalDateTime parseDateTime(String value, boolean isFrom) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String v = StringUtils.trim(value);

        // Try full datetime with millis
        try {
            return LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        } catch (DateTimeParseException ignored) {
        }

        // Try datetime without millis
        try {
            return LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }

        // Try date only
        try {
            LocalDate d = LocalDate.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (isFrom) {
                return d.atStartOfDay();
            }
            return d.atTime(LocalTime.of(23, 59, 59, 999_000_000));
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }
}
