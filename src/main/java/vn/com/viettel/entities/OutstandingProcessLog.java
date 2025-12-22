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
@Table(name = "OUTSTANDING_PROCESS_LOG")
public class OutstandingProcessLog {
    @Id
    @Column(name = "PROCESS_ID", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "OUTSTANDING_ID", nullable = false)
    private Long outstandingId;

    @Size(max = 30)
    @NotNull
    @Column(name = "ACTION_TYPE", nullable = false, length = 30)
    private String actionType;

    @Size(max = 2000)
    @NotNull
    @Column(name = "PROCESS_CONTENT", nullable = false, length = 2000)
    private String processContent;

    @NotNull
    @Column(name = "ASSIGNED_USER_ID", nullable = false)
    private Long assignedUserId;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "PROCESSED_AT", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
