package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "OUTSTANDING_WORK_ITEM")
@SequenceGenerator(
        name = "outstanding_work_item_seq_gen",
        sequenceName = "SEQ_OUTSTANDING_WORK_ITEM",
        allocationSize = 1
)
public class OutstandingWorkItem {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outstanding_work_item_seq_gen")
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
