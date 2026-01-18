package vn.com.viettel.auth.constant;

import lombok.Getter;

@Getter
public enum AuthErrorCode {
    USER_LOCKED(403),
    USER_NOT_FOUND(404),
    PASSWORD_EXPIRED(40301),
    BRUTE_FORCE_LOCKED(423),
    INVALID_CREDENTIALS(401);

    private final int code;
    AuthErrorCode(int code) { this.code = code; }
}
