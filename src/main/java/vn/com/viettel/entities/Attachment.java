package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ATTACHMENT")
public class Attachment {
    @Id
    @Column(name = "ATTACHMENT_ID", nullable = false)
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

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "UPLOADED_AT", nullable = false)
    private Instant uploadedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
