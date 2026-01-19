package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CAT_MANUFACTURER")
public class CatManufacturer {

    @Id
    @Column(name = "MANUFACTURER_ID", nullable = false)
    private Long manufacturerId;

    @Column(name = "MANUFACTURER_CODE", nullable = false, length = 50)
    private String manufacturerCode;

    @Column(name = "MANUFACTURER_NAME", nullable = false, length = 250)
    private String manufacturerName;

    @Column(name = "COUNTRY", length = 100)
    private String country;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    private Boolean isActive;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED", nullable = false, length = 1)
    private Boolean isDeleted;
}
