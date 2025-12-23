package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_ACCEPTANCE")
@SequenceGenerator(
        name = "outstanding_acceptance_seq_gen",
        sequenceName = "SEQ_OUTSTANDING_ACCEPTANCE",
        allocationSize = 1
)
public class OutstandingAcceptance {
    @Id
    @Column(name = "ACCEPTANCE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outstanding_acceptance_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "OUTSTANDING_ID", nullable = false)
    private Long outstandingId;

    @Size(max = 20)
    @NotNull
    @Column(name = "RESULT", nullable = false, length = 20)
    private String result;

    @Size(max = 2000)
    @NotNull
    @Column(name = "ACCEPTANCE_NOTE", nullable = false, length = 2000)
    private String acceptanceNote;

    @NotNull
    @Column(name = "ACCEPTED_BY", nullable = false)
    private Long acceptedBy;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "ACCEPTED_AT", nullable = false)
    private LocalDateTime acceptedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

}
