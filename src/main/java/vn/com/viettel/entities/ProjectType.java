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
@Table(name = "PROJECT_TYPE")
@SequenceGenerator(
        name = "project_type_seq_gen",
        sequenceName = "SEQ_PROJECT_TYPE",
        allocationSize = 1
)
public class ProjectType {
    @Id
    @Column(name = "PROJECT_TYPE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_type_seq_gen")
    private Long id;

    @Size(max = 250)
    @NotNull
    @Column(name = "PROJECT_TYPE_NAME", nullable = false, length = 250)
    private String projectTypeName;

    @Column(name = "UPDATED_BY")
    private Long updatedByUserId;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdByUserId;

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
