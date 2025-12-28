package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "RECOMMENDATION")
@SequenceGenerator(
        name = "recommendation_seq_gen",
        sequenceName = "SEQ_KN_KIEN_NGHI",
        allocationSize = 1
)
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recommendation_seq_gen")
    @Column(name = "RECOMMENDATION_ID", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "RECOMMENDATION_CODE", length = 50)
    private String recommendationCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "RECOMMENDATION_TITLE", nullable = false, length = 250)
    private String recommendationTitle;

    @Size(max = 500)
    @NotNull
    @Column(name = "CONTENT", nullable = false, length = 500)
    private String content;


    @Column(name = "PROJECT_ID")
    private Long projectId;


    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "PHASE_ID")
    private Long phaseId;

    @NotNull
    @Column(name = "RECOMMENDATION_TYPE_ID", nullable = false)
    private Long recommendationTypeId;

    @Size(max = 20)
    @NotNull
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private String priority;

    @NotNull
    @Column(name = "DEADLINE", nullable = false)
    private LocalDate deadline;

    @Size(max = 20)
    @NotNull
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "CREATED_BY")
    private Long createdById;

    @Column(name = "CREATED_ORG_ID")
    private Long createdOrgId;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    @Column(name = "CLOSED_BY")
    private Long closedById;

    @Column(name = "CURRENT_PROCESS_BY")
    private Long currentProcessById;

    @NotNull
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @ColumnDefault("'N'")
    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY")
    private Long deletedById;

    @Column(name = "LAST_UPDATE")
    private LocalDateTime lastUpdate;

    @Column(name = "LAST_UPDATE_BY")
    private Long lastUpdateBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECOMMENDATION_TYPE_ID", insertable = false, updatable = false)
    private CatRecommendationType catRecommendationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", insertable = false, updatable = false)
    private SysUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENT_PROCESS_BY", insertable = false, updatable = false)
    private SysUser currentProcessBy;
}
