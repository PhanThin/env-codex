package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RECOMMENDATION_SOURCE_REL")
public class RecommendationSourceRel {
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "RECOMMENDATION_ID", nullable = false)
    private Long recommendationId;

    @NotNull
    @Column(name = "SOURCE_ID", nullable = false)
    private Long sourceId;


}
