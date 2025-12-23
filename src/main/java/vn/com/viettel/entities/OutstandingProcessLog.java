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
@SequenceGenerator(
        name = "outstanding_process_log_seq_gen",
        sequenceName = "SEQ_OUTSTANDING_PROCESS_LOG",
        allocationSize = 1
)
public class OutstandingProcessLog {
    @Id
    @Column(name = "PROCESS_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outstanding_process_log_seq_gen")
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

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
