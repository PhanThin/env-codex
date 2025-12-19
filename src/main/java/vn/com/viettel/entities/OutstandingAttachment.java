package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_ATTACHMENT")
public class OutstandingAttachment {
    @Id
    @Column(name = "ATTACHMENT_ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "OUTSTANDING_ID", nullable = false)
    private OutstandingItem outstanding;

    @Size(max = 255)
    @NotNull
    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;

    @Size(max = 1000)
    @NotNull
    @Column(name = "FILE_URL", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "UPLOADED_BY", nullable = false)
    private SysUser uploadedBy;

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
