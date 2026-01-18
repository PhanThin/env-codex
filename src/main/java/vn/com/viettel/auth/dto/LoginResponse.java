package vn.com.viettel.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String message;
    private Integer userType;
    private Boolean requirePasswordChange;
    private Long lockTimeRemaining;
}
