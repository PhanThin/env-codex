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
@Table(name = "RECOMMENDATION")
public class Recommendation {
    @Id
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

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "ITEM_ID")
    private ProjectItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "PHASE_ID")
    private CatProjectPhase phase;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "WORK_ITEM_ID")
    private WorkItem workItem;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "RECOMMENDATION_TYPE_ID", nullable = false)
    private CatRecommendationType recommendationType;

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

    @Column(name = "CLOSED_AT")
    private Instant closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "CLOSED_BY")
    private SysUser closedBy;

    @NotNull
    @ColumnDefault("'N'")
    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted;

    @Column(name = "DELETED_AT")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "DELETED_BY")
    private SysUser deletedBy;

    @Column(name = "LAST_UPDATE")
    private Instant lastUpdate;

    @Column(name = "LAST_UPDATE_BY")
    private Long lastUpdateBy;


}
