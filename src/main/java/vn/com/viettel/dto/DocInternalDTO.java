package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import vn.com.viettel.entities.DocInternal;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocInternalDTO {
    Long id;

    String docSyncId;
    @NotBlank
    @Length(min = 1, max = 200)
    String code;

    @NotBlank
    @Length(min = 1, max = 1000)
    String title;
    @NotNull
    Long type;

    @NotNull
    Long orgOwn;

    @NotNull
    Long topic;

    @NotNull
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date issuedDate;

    @NotNull
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date effectiveDate;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date expireDate;

    @NotBlank
    @Range
    String effectiveStatus;

    Long createdType;

    Long isDelete;

    Date createdDate;

    String createdBy;

    Date modifiedDate;

    String modifiedBy;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date issueDateFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date effectiveDateFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date expireDateFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date issueDateTo;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date effectiveDateTo;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "vi_VN", timezone = "Asia/Ho_Chi_Minh")
    Date expireDateTo;

    String typeName;

    String orgOwnName;

    String topicName;

    Integer page;
    Integer totalNV;
    Integer totalMapping;
    Integer totalApproved;

    String mappingStatus;
    String buildingStatus;

    public DocInternalDTO(String code, String title, String typeName, String orgOwnName, String topicName,
                          Date issuedDate, Date effectiveDate, String effectiveStatus, Date expireDate) {
        this.code = code;
        this.title = title;
        this.typeName = typeName;
        this.orgOwnName = orgOwnName;
        this.topicName = topicName;
        this.issuedDate = issuedDate;
        this.effectiveDate = effectiveDate;
        this.effectiveStatus = effectiveStatus;
        this.expireDate = expireDate;
    }

    public DocInternalDTO(BigDecimal id, String code, String title, BigDecimal effectiveStatus, BigDecimal totalNV, BigDecimal totalMapping, BigDecimal totalApproved) {
        this.id = id.longValue();
        this.code = code;
        this.title = title;
        this.effectiveStatus = effectiveStatus.toString();
        this.totalNV = totalNV.intValue();
        this.totalMapping = totalMapping.intValue();
        this.totalApproved = totalApproved.intValue();
    }

    public DocInternal toEntity() {
        DocInternal docInternal = new DocInternal();
        docInternal.setCode(code.trim());
        docInternal.setTitle(title.trim());
        docInternal.setType(type);
        docInternal.setOrgOwn(orgOwn);
        docInternal.setTopic(topic);
        docInternal.setIssuedDate(new java.sql.Date(issuedDate.getTime()));
        docInternal.setEffectiveDate(new java.sql.Date(effectiveDate.getTime()));
        if (expireDate != null) docInternal.setExpireDate(new java.sql.Date(expireDate.getTime()));
        docInternal.setEffectiveStatus(effectiveStatus);
        return docInternal;
    }
}
