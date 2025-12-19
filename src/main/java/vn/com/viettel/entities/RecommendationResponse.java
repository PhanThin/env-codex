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
@Table(name = "RECOMMENDATION_RESPONSE")
public class RecommendationResponse {
    @Id
    @Column(name = "RESPONSE_ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "RECOMMENDATION_ID", nullable = false)
    private Recommendation recommendation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "RESPONDED_BY", nullable = false)
    private SysUser respondedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "RESPONDED_ORG_ID")
    private SysOrg respondedOrg;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "RESPONDED_AT", nullable = false)
    private Instant respondedAt;

    @Size(max = 2000)
    @NotNull
    @Column(name = "RESPONSE_CONTENT", nullable = false, length = 2000)
    private String responseContent;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
