package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import vn.com.viettel.entities.SysCategoryAction;

import java.math.BigDecimal;
import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysCategoryActionDTO {
    Long id;

    String codeLevel1;
    String nameLevel1;

    String codeLevel2;
    String nameLevel2;

    String code;

    @NotBlank
    @Length(min = 1, max = 1000)
    String name;

    String parentCode;

    @NotNull
    @Range(min = 0, max = 1)
    String status;

    Date createdDate;

    String createdBy;

    Date modifiedDate;

    String modifiedBy;

    Integer page;

    public SysCategoryAction toEntity() {
        SysCategoryAction sysCategoryAction = new SysCategoryAction();
        sysCategoryAction.setName(name);
        sysCategoryAction.setParentCode(parentCode);
        sysCategoryAction.setStatus(status);
        return sysCategoryAction;
    }

    public SysCategoryActionDTO(Long id, String code, String name, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
    }

    public SysCategoryActionDTO(BigDecimal id, String codeLevel1, String nameLevel1, String codeLevel2, String nameLevel2, BigDecimal status,
                                java.util.Date createdDate, String createdBy, java.util.Date modifiedDate, String modifiedBy) {
        this.id = id.longValue();
        this.codeLevel1 = codeLevel1;
        this.nameLevel1 = nameLevel1;
        this.codeLevel2 = codeLevel2;
        this.nameLevel2 = nameLevel2;
        this.status = status.toString();
        this.createdDate = new Date(createdDate.getTime());
        this.createdBy = createdBy;
        this.modifiedDate = new Date(modifiedDate.getTime());
        this.modifiedBy = modifiedBy;
    }
}
