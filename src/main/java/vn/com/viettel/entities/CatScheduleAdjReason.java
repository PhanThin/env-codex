package vn.com.viettel.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "CAT_SCHEDULE_ADJ_REASON")
@SequenceGenerator(
        name = "cat_schedule_adj_reason_seq_gen",
        sequenceName = "SEQ_CAT_SCHEDULE_ADJ_REASON",
        allocationSize = 1
)
public class CatScheduleAdjReason {

    @Id
    @Column(name = "REASON_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_schedule_adj_reason_seq_gen")
    private Long reasonId;

    @Column(name = "REASON_CODE")
    private String reasonCode;

    @Column(name = "REASON_NAME")
    private String reasonName;

    @Column(name = "NOTE")
    private String note;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}
