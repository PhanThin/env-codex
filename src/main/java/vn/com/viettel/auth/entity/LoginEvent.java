package vn.com.viettel.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginEvent {
    private final String username;
    private final boolean success;
    private final String message;
    private final String ip;
    private final String userAgent;
}