package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_ITEM")
public class OutstandingItem {
    @Id
    @Column(name = "OUTSTANDING_ID", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "OUTSTANDING_CODE", nullable = false, length = 50)
    private String outstandingCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "ITEM_ID")
    private ProjectItem item;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "WORK_ITEM_ID", nullable = false)
    private WorkItem workItem;

    @Size(max = 50)
    @Column(name = "ACCEPTANCE_TYPE", length = 50)
    private String acceptanceType;

    @Column(name = "ACCEPTANCE_REF_ID")
    private Long acceptanceRefId;

    @Size(max = 1000)
    @Column(name = "ACCEPTANCE_FILE_URL", length = 1000)
    private String acceptanceFileUrl;

    @Size(max = 250)
    @NotNull
    @Column(name = "OUTSTANDING_TITLE", nullable = false, length = 250)
    private String outstandingTitle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "OUTSTANDING_TYPE_ID", nullable = false)
    private CatOutstandingType outstandingType;

    @Size(max = 20)
    @NotNull
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private String priority;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private SysUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "CREATED_ORG_ID")
    private SysOrg createdOrg;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "ASSIGNED_USER_ID", nullable = false)
    private SysUser assignedUser;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "ASSIGNED_ORG_ID", nullable = false)
    private SysOrg assignedOrg;

    @NotNull
    @Column(name = "DEADLINE", nullable = false)
    private LocalDate deadline;

    @Size(max = 30)
    @NotNull
    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @NotNull
    @ColumnDefault("'N'")
    @Column(name = "IS_LOCKED", nullable = false)
    private Boolean isLocked;

    @Column(name = "LAST_UPDATE")
    private Instant lastUpdate;

    @Column(name = "LAST_UPDATE_BY")
    private Long lastUpdateBy;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
