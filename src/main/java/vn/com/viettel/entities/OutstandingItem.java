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
public class OutstandingItem {
    @Id
    @Column(name = "OUTSTANDING_ID", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "OUTSTANDING_CODE", length = 50)
    private String outstandingCode;

    @NotNull
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @JoinColumn(name = "ITEM_ID") //FK table ProjectItem
    private Long itemId;

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
    @Column(name = "OUTSTANDING_TYPE_ID", nullable = false) // FK table CatOutstandingType
    private Long outstandingTypeId;

    @Size(max = 20)
    @NotNull
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private String priority;

    @NotNull
    @Column(name = "CREATED_BY", nullable = false)
    private Long createdBy;

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
    private LocalDateTime lastUpdate;

    @Column(name = "LAST_UPDATE_BY")
    private Long lastUpdateBy;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
