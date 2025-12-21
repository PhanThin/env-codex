package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "RECOMMENDATION_ASSIGNMENT")
public class RecommendationAssignment {
    @Id
    @Column(name = "ID", nullable = false)
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
    @ColumnDefault("'N'")
    @Column(name = "IS_PRIMARY", nullable = false)
    private Boolean isPrimary;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "ASSIGNED_AT", nullable = false)
    private Instant assignedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
