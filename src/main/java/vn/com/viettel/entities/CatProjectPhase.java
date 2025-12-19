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
@Table(name = "CAT_PROJECT_PHASE")
public class CatProjectPhase {
    @Id
    @Column(name = "PHASE_ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @Size(max = 50)
    @NotNull
    @Column(name = "PHASE_CODE", nullable = false, length = 50)
    private String phaseCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "PHASE_NAME", nullable = false, length = 250)
    private String phaseName;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @NotNull
    @ColumnDefault("'Y'")
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
