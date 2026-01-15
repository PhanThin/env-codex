package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "WORK_ITEM")
@SequenceGenerator(
        name = "work_item_seq_gen",
        sequenceName = "SEQ_WORK_ITEM",
        allocationSize = 1
)
public class WorkItem {
    @Id
    @Column(name = "WORK_ITEM_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_item_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "ITEM_ID", nullable = false)
    private Long itemId;

    @Size(max = 250)
    @NotNull
    @Column(name = "WORK_ITEM_CODE", nullable = false, length = 250)
    private String workItemCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "WORK_ITEM_NAME", nullable = false, length = 250)
    private String workItemName;

    @Size(max = 500)
    @Column(name = "NOTE", nullable = false, length = 500)
    private String note;

    @NotNull
    @Column(name = "CAT_WORK_ITEM_ID", nullable = false)
    private Long catWorkItemId;

    @Column(name = "UPDATED_BY")
    private Long updatedByUserId;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdByUserId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @NotNull
    @Column(name = "PROJECT_PHASE_ID", nullable = false)
    private Long projectPhaseId;

    @NotNull
    @Column(name = "UNIT_ID", nullable = false)
    private Long unitId;

    @NotNull
    @Column(name = "PROJECT_TYPE_ID", nullable = false)
    private Long projectTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_PHASE_ID", insertable = false, updatable = false)
    private CatProjectPhase projectPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", insertable = false, updatable = false)
    private ProjectItem projectItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_TYPE_ID", insertable = false, updatable = false)
    private ProjectType projectType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_ID", insertable = false, updatable = false)
    private CatUnit unit;
}
