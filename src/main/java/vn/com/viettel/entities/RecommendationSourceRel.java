package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RECOMMENDATION_SOURCE_REL")
@SequenceGenerator(
        name = "recommendation_source_rel_seq_gen",
        sequenceName = "SEQ_RECOMMENDATION_SOURCE_REL",
        allocationSize = 1
)
public class RecommendationSourceRel {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recommendation_source_rel_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "RECOMMENDATION_ID", nullable = false)
    private Long recommendationId;

    @NotNull
    @Column(name = "SOURCE_ID", nullable = false)
    private Long sourceId;


}
