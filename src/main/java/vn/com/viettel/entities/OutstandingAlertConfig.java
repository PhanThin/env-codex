package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.YesNoConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_ALERT_CONFIG")
@SequenceGenerator(
        name = "outstanding_alert_config_seq_gen",
        sequenceName = "SEQ_OUTSTANDING_ALERT_CONFIG",
        allocationSize = 1
)
public class OutstandingAlertConfig {
    @Id
    @Column(name = "CONFIG_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outstanding_alert_config_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "OUTSTANDING_ID", nullable = false)
    private Long outstandingId;

    @NotNull
    @Column(name = "LEVEL_NO", nullable = false)
    private Integer levelNo;

    @NotNull
    @Column(name = "PERCENT_TIME", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentTime;

    @NotNull
    @JdbcTypeCode(java.sql.Types.CHAR)
    @ColumnDefault("'Y'")
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "CREATED_BY")
    private SysUser createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
