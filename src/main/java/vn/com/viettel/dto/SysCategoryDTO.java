package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import vn.com.viettel.entities.SysCategory;

import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysCategoryDTO {
    Long id;

    String code;

    @NotBlank
    @Length(min = 1, max = 1000)
    String name;
    @NotNull
    @Range(min = 0, max = 1)
    String status;

    @NotNull
    @Range(min = 1, max = 4)
    Short type;

    Date createdDate;

    String createdBy;

    Date modifiedDate;

    String modifiedBy;

    Integer page;

    public SysCategory toEntity() {
        SysCategory sysCategory = new SysCategory();
        sysCategory.setName(name.trim());
        sysCategory.setStatus(status);
        sysCategory.setType(type);
        return sysCategory;
    }

    public SysCategoryDTO(Long id, String code, String name, Short type) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
    }
}
