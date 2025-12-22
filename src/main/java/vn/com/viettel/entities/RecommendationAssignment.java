package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.YesNoConverter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "RECOMMENDATION_ASSIGNMENT")
@SequenceGenerator(
        name = "recommendation_assignment_seq_gen",
        sequenceName = "SEQ_RECOMMENDATION_ASSIGNMENT",
        allocationSize = 1
)
public class RecommendationAssignment {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recommendation_assignment_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "RECOMMENDATION_ID", nullable = false)
    private Long recommendationId;

    @NotNull
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "ORG_ID")
    private Long org;

    @NotNull
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @ColumnDefault("'N'")
    @Column(name = "IS_PRIMARY", nullable = false)
    private Boolean isPrimary;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "ASSIGNED_AT", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
