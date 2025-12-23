package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "CAT_OUTSTANDING_TYPE")
@SequenceGenerator(
        name = "outstanding_type_seq_gen",
        sequenceName = "SEQ_CAT_OUTSTANDING_TYPE",
        allocationSize = 1
)
public class CatOutstandingType {
    @Id
    @Column(name = "OUTSTANDING_TYPE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outstanding_type_seq_gen")
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "TYPE_CODE", nullable = false, length = 50)
    private String typeCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "TYPE_NAME", nullable = false, length = 250)
    private String typeName;

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

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;


}
