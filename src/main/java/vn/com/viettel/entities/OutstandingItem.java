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
@Table(name = "OUTSTANDING_ITEM")
@SequenceGenerator(
        name = "outstanding_item_seq_gen",
        sequenceName = "SEQ_OUTSTANDING_ITEM",
        allocationSize = 1
)
public class OutstandingItem {
    @Id
    @Column(name = "OUTSTANDING_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outstanding_item_seq_gen")
    private Long id;

    @Size(max = 50)
    @Column(name = "OUTSTANDING_CODE", length = 50)
    private String outstandingCode;

    @NotNull
    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @Column(name = "PHASE_ID", nullable = false)
    private Long phaseId;

    @Column(name = "ITEM_ID") //FK table ProjectItem
    private Long itemId;

    @NotNull
    @Column(name = "WORK_ITEM_ID", nullable = false) //FK table WorkItem
    private Long workItemId;

    @Size(max = 50)
    @Column(name = "ACCEPTANCE_TYPE", length = 50)
    private String acceptanceType;

    @Column(name = "ACCEPTANCE_REF_ID")
    private String acceptanceRefId;

    @Size(max = 1000)
    @Column(name = "ACCEPTANCE_FILE_URL", length = 1000)
    private String acceptanceFileUrl;

    @Size(max = 250)
    @NotNull
    @Column(name = "OUTSTANDING_TITLE", nullable = false, length = 250)
    private String outstandingTitle;

    @NotNull
    @Column(name = "OUTSTANDING_TYPE_ID", nullable = false) // FK table CatOutstandingType
    private Long outstandingTypeId;

    @Size(max = 20)
    @NotNull
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private String priority;

    @NotNull
    @Column(name = "CREATED_BY", nullable = false)
    private Long createdById;

    @Column(name = "CREATED_ORG_ID")
    private Long createdOrgId;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "ASSIGNED_USER_ID", nullable = false)
    private Long assignedUserId;

    @NotNull
    @Column(name = "ASSIGNED_ORG_ID", nullable = false)
    private Long assignedOrgId;

    @NotNull
    @Column(name = "DEADLINE", nullable = false)
    private LocalDate deadline;

    @Size(max = 30)
    @NotNull
    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @NotNull
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @ColumnDefault("'N'")
    @Column(name = "IS_LOCKED", nullable = false)
    private Boolean isLocked;

    @Column(name = "LAST_UPDATE")
    private LocalDateTime lastUpdateAt;

    @Column(name = "LAST_UPDATE_BY")
    private Long lastUpdateBy;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", insertable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PHASE_ID", insertable = false, updatable = false)
    private CatProjectPhase phase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", insertable = false, updatable = false)
    private ProjectItem projectItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORK_ITEM_ID", insertable = false, updatable = false)
    private WorkItem workItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTSTANDING_TYPE_ID", insertable = false, updatable = false)
    private CatOutstandingType outstandingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", insertable = false, updatable = false)
    private SysUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_USER_ID", insertable = false, updatable = false)
    private SysUser assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_ORG_ID", insertable = false, updatable = false)
    private SysOrg assignedOrg;
}
