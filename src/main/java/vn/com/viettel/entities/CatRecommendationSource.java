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
@Table(name = "CAT_RECOMMENDATION_SOURCE")
@SequenceGenerator(
        name = "recommendation_source_seq_gen",
        sequenceName = "SEQ_CAT_RECOMMENDATION_SOURCE",
        allocationSize = 1
)

public class CatRecommendationSource {
    @Id
    @Column(name = "SOURCE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recommendation_source_seq_gen")
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "SOURCE_CODE", nullable = false, length = 50)
    private String sourceCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "SOURCE_NAME", nullable = false, length = 250)
    private String sourceName;

    @Size(max = 500)
    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @NotNull
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @ColumnDefault("'Y'")
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
