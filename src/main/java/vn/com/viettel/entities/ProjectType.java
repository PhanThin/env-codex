package vn.com.viettel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity for table PROJECT_TYPE.
 *
 * Global rules:
 * - Entity only contains columns in table
 * - No relationship mapping
 * - Date/time uses LocalDateTime
 */
@Getter
@Setter
@Entity
@Table(name = "PROJECT_TYPE")
public class ProjectType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PROJECT_TYPE_SEQ")
    @SequenceGenerator(name = "PROJECT_TYPE_SEQ", sequenceName = "SEQ_PROJECT_TYPE", allocationSize = 1)
    @Column(name = "PROJECT_TYPE_ID", nullable = false)
    private Long id;

    @Column(name = "PROJECT_TYPE_NAME", nullable = false, length = 250)
    private String projectTypeName;

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
