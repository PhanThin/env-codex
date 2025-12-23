package vn.com.viettel.entities;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link OutstandingAlertConfig}
 */
@Getter
@Setter
public class OutstandingAlertConfigDto implements Serializable {
    Long id;
    @NotNull
    Long outstandingId;
    @NotNull
    Integer levelNo;
    @NotNull
    BigDecimal percentTime;
    @NotNull
    LocalDate alertDate;
    @NotNull
    Boolean isActive;
    @NotNull
    LocalDateTime createdAt;
    Long createdBy;
}
