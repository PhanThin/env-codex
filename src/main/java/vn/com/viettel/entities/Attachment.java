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
@Table(name = "ATTACHMENT")
@SequenceGenerator(
        name = "attachment_seq_gen",
        sequenceName = "SEQ_KN_ATTACHMENT",
        allocationSize = 1
)
public class Attachment {
    @Id
    @Column(name = "ATTACHMENT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_seq_gen")
    private Long id;

    @NotNull
    @Column(name = "REFERENCE_ID", nullable = false)
    private Long referenceId;

    @Size(max = 50)
    @NotNull
    @Column(name = "REFERENCE_TYPE", nullable = false, length = 50)
    private String referenceType;

    @Size(max = 255)
    @NotNull
    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;

    @Size(max = 20)
    @Column(name = "FILE_EXT", length = 20)
    private String fileExt;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Size(max = 1000)
    @NotNull
    @Column(name = "FILE_URL", nullable = false, length = 1000)
    private String fileUrl;

    @Size(max = 500)
    @NotNull
    @Column(name = "FILE_PATH", nullable = false, length = 1000)
    private String filePath;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "UPLOADED_AT", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "UPLOADED_BY")
    private Long uploadedBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
