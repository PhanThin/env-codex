package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "SYS_USER")
@SequenceGenerator(
        name = "sys_user_seq_gen",
        sequenceName = "SEQ_SYS_USER",
        allocationSize = 1
)
public class SysUser {
    @Id
    @Column(name = "USER_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_user_seq_gen")
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "USERNAME", nullable = false, length = 100, unique = true)
    private String username;

    @Size(max = 250)
    @NotNull
    @Column(name = "FULL_NAME", nullable = false, length = 250)
    private String fullName;

    @Size(max = 250)
    @Column(name = "TITLE", length = 250)
    private String title;

    @Column(name = "ORG_ID")
    private Long orgId;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_DELETED")
    private Boolean isDeleted;

    @Column(name = "TYPE")
    private Integer type; // 0: IMIS, 1: Nội bộ

    @Column(name = "LAST_PASSWORD_CHANGE")
    private LocalDateTime lastPasswordChange;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_LOCKED_TEMPORARILY")
    private Boolean isLockedTemporarily;
}
