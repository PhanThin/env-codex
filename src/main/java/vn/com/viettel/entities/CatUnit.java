package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "CAT_UNIT")
@SequenceGenerator(
        name = "cat_unit_gen",
        sequenceName = "SEQ_CAT_UNIT",
        allocationSize = 1
)
public class CatUnit {
    @Id
    @Column(name = "UNIT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_unit_gen")
    private Long id;

    @Size(max = 50)
    @Column(name = "UNIT_NAME", length = 50)
    private String unitName;

    @Size(max = 50)
    @Column(name = "UNIT_TYPE", length = 50)
    private String unitType;

    @Column(name = "CREATED_BY")
    private Long createdByUserId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @NotNull
    @ColumnDefault("'Y'")
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @ColumnDefault("'N'")
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @Column(name = "UPDATED_BY")
    private Long updatedByUserId;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;


}
