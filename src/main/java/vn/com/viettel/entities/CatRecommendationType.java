package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "CAT_RECOMMENDATION_TYPE")
@SequenceGenerator(
        name = "recommendation_type_seq_gen",
        sequenceName = "SEQ_CAT_RECOMMENDATION_TYPE",
        allocationSize = 1
)
public class CatRecommendationType {
    @Id
    @Column(name = "RECOMMENDATION_TYPE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recommendation_type_seq_gen")
    private Long id;

    @Size(max = 50)
    @Column(name = "TYPE_CODE", length = 50, nullable = true)
    private String typeCode;

    @Size(max = 250)
    @Column(name = "TYPE_NAME", nullable = false, length = 250)
    private String typeName;

    @Size(max = 500)
    @Column(name = "NOTE", length = 500, nullable = true)
    private String note;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;
}
