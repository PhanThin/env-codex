package vn.com.viettel.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.YesNoConverter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SYS_LOGIN_LOG")
@SequenceGenerator(
        name = "sys_login_log_seq_gen",
        sequenceName = "SEQ_SYS_LOGIN_LOG",
        allocationSize = 1
)
public class SysLoginLog {
    @Id
    @Column(name = "LOG_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_login_log_seq_gen")
    private Long id;

    @Column(name = "USERNAME", length = 100)
    private String username;

    @Column(name = "LOGIN_TIME")
    private LocalDateTime loginTime;

    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    @JdbcTypeCode(java.sql.Types.CHAR)
    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_SUCCESS")
    private Boolean isSuccess;

    @Column(name = "ERROR_MESSAGE", length = 500)
    private String errorMessage;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;
}