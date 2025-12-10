package vn.com.viettel.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.com.viettel.dto.DocInternalDTO;

import java.math.BigDecimal;
import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "DOC_INTERNAL")
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SqlResultSetMapping(
        name = "DocInternalDTOMapping",
        classes = @ConstructorResult(
                targetClass = DocInternalDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = BigDecimal.class),
                        @ColumnResult(name = "code", type = String.class),
                        @ColumnResult(name = "title", type = String.class),
                        @ColumnResult(name = "effectiveStatus", type = BigDecimal.class),
                        @ColumnResult(name = "totalNV", type = BigDecimal.class),
                        @ColumnResult(name = "totalMapping", type = BigDecimal.class),
                        @ColumnResult(name = "totalApproved", type = BigDecimal.class),
                }
        )
)
public class DocInternal {
    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "DOC_SYNC_ID")
    String docSyncId;

    @Column(name = "CODE", nullable = false, length = 200)
    String code;

    @Column(name = "TITLE", nullable = false, length = 1000)
    String title;

    @Column(name = "TYPE")
    Long type;

    @Column(name = "ORG_OWN")
    Long orgOwn;

    @Column(name = "TOPIC")
    Long topic;

    @Column(name = "ISSUED_DATE")
    Date issuedDate;

    @Column(name = "EFFECTIVE_DATE")
    Date effectiveDate;

    @Column(name = "EXPIRE_DATE")
    Date expireDate;

    @Column(name = "EFFECTIVE_STATUS")
    String effectiveStatus;

    @Column(name = "CREATED_TYPE")
    Long createdType;

    @Column(name = "IS_DELETE")
    Long isDelete;

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

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public enum IsDelete {
        NO(0L),
        YES(1L);
        public final Long value;

        IsDelete(Long value) {
            this.value = value;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public enum CreatedType {
        KAFKA(0L),
        API(1L),
        MANUAL(2L);
        public final Long value;

        CreatedType(Long value) {
            this.value = value;
        }
    }
}
