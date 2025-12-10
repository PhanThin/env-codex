package vn.com.viettel.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.com.viettel.dto.SysCategoryActionDTO;

import java.math.BigDecimal;
import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "SYS_CATEGORY_ACTION")
@FieldDefaults(level = AccessLevel.PRIVATE)
@SqlResultSetMapping(
        name = "SysCategoryActionDTOMapping",
        classes = @ConstructorResult(
                targetClass = SysCategoryActionDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = BigDecimal.class),
                        @ColumnResult(name = "codeLevel1", type = String.class),
                        @ColumnResult(name = "nameLevel1", type = String.class),
                        @ColumnResult(name = "codeLevel2", type = String.class),
                        @ColumnResult(name = "nameLevel2", type = String.class),
                        @ColumnResult(name = "status", type = BigDecimal.class),
                        @ColumnResult(name = "createdDate", type = java.util.Date.class),
                        @ColumnResult(name = "createdBy", type = String.class),
                        @ColumnResult(name = "modifiedDate", type = java.util.Date.class),
                        @ColumnResult(name = "modifiedBy", type = String.class),
                }
        )
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysCategoryAction {
    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "CODE", nullable = false, length = 36)
    String code;

    @Column(name = "NAME", nullable = false, length = 1000)
    String name;

    @Column(name = "PARENT_CODE", length = 36)
    String parentCode;

    @Column(name = "STATUS")
    String status;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    Date createdDate;

    @Column(name = "CREATED_BY")
    String createdBy;

    @UpdateTimestamp
    @Column(name = "MODIFIED_DATE")
    Date modifiedDate;

    @Column(name = "MODIFIED_BY")
    String modifiedBy;
}
