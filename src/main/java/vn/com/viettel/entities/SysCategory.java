package vn.com.viettel.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "SYS_CATEGORY")
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysCategory {
    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "CODE", nullable = false, length = 36)
    String code;

    @Column(name = "NAME", nullable = false, length = 1000)
    String name;

    @Column(name = "STATUS")
    String status;

    @Column(name = "TYPE", nullable = false)
    Short type;

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
    public enum Type {
        TOPIC((short) 1),
        CAP_THKS((short) 2),
        DV_SO_HUU((short) 3),
        LOAI_VB((short) 4);
        public final short value;

        Type(short value) {
            this.value = value;
        }
    }
}
