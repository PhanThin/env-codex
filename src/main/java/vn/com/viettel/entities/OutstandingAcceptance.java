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

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_ACCEPTANCE")
public class OutstandingAcceptance {
    @Id
    @Column(name = "ACCEPTANCE_ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "OUTSTANDING_ID", nullable = false)
    private OutstandingItem outstanding;

    @Size(max = 20)
    @NotNull
    @Column(name = "RESULT", nullable = false, length = 20)
    private String result;

    @Size(max = 2000)
    @NotNull
    @Column(name = "ACCEPTANCE_NOTE", nullable = false, length = 2000)
    private String acceptanceNote;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "ACCEPTED_BY", nullable = false)
    private SysUser acceptedBy;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "ACCEPTED_AT", nullable = false)
    private Instant acceptedAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;


}
