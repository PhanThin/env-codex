package vn.com.viettel.dto.auth;

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
}
