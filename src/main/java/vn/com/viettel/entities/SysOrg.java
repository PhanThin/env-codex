package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "SYS_ORG")
public class SysOrg {
    @Id
    @Column(name = "ORG_ID", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ORG_CODE", nullable = false, length = 50)
    private String orgCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "ORG_NAME", nullable = false, length = 250)
    private String orgName;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
