package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "CATEGORY_WORK_ITEM")
@SequenceGenerator(
        name = "cat_work_item_seq_gen",
        sequenceName = "SEQ_CATEGORY_WORK_ITEM",
        allocationSize = 1
)
public class CategoryWorkItem {
    @Id
    @Column(name = "CATEGORY_WORK_ITEM_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_work_item_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "PROJECT_ITEM_ID", nullable = false)
    private Long projectItemId;

    @NotNull
    @Column(name = "PROJECT_PHASE_ID", nullable = false)
    private Long projectPhaseId;

    @NotNull
    @Column(name = "UNIT_ID", nullable = false)
    private Long unitId;

    @Size(max = 250)
    @NotNull
    @Column(name = "CATEGORY_WORK_ITEM_CODE", nullable = false, length = 250)
    private String categoryWorkItemCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "CATEGORY_WORK_ITEM_NAME", nullable = false, length = 250)
    private String categoryWorkItemName;

    @Column(name = "UPDATED_BY")
    private Long updatedByUserId;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdByUserId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @NotNull
    @ColumnDefault("'Y'")
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @NotNull
    @ColumnDefault("'N'")
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted;

    @Size(max = 500)
    @Column(name = "NOTE", length = 500)
    private String note;

    @NotNull
    @Column(name = "PROJECT_TYPE_ID", nullable = false)
    private Long projectTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_PHASE_ID", insertable = false, updatable = false)
    private CatProjectPhase catProjectPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ITEM_ID", insertable = false, updatable = false)
    private ProjectItem catProjectItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_TYPE_ID", insertable = false, updatable = false)
    private ProjectType catProjectType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_ID", insertable = false, updatable = false)
    private CatUnit catUnit;

}
