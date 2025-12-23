package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "RECOMMENDATION_RESPONSE")
@SequenceGenerator(
        name = "recommendation_response_seq_gen",
        sequenceName = "SEQ_RECOMMENDATION_RESPONSE",
        allocationSize = 1
)
public class RecommendationResponse {
    @Id
    @Column(name = "RESPONSE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recommendation_response_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "RECOMMENDATION_ID", nullable = false)
    private Long recommendationId;

    @NotNull
    @Column(name = "RESPONDED_BY", nullable = false)
    private Long respondedBy;

    @Column(name = "RESPONDED_ORG_ID")
    private Long respondedOrgId;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "RESPONDED_AT", nullable = false)
    private LocalDateTime respondedAt;

    @Size(max = 2000)
    @NotNull
    @Column(name = "RESPONSE_CONTENT", nullable = false, length = 2000)
    private String responseContent;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
