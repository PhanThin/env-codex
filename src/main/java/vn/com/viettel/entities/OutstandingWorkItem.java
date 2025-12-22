package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_WORK_ITEM")
public class OutstandingWorkItem {
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "OUTSTANDING_ID", nullable = false)
    private Long outstandingId;

    @NotNull
    @Column(name = "WORK_ITEM_ID", nullable = false)
    private Long workItemId;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
