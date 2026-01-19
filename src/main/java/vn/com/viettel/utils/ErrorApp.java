package vn.com.viettel.utils;

public enum ErrorApp {
    SUCCESS(200, "msg.success"),
    BAD_REQUEST(400, "msg.bad.request"),
    BAD_REQUEST_PATH(400, "msg.bad.request.path"),
    BAD_NOT_FOUND(404, "msg.not.found"),
    UNAUTHORIZED(401, "msg.unauthorized"),
    FORBIDDEN(403, "msg.access.denied"),
    CONFLICT(409, "msg.conflict"),
    INTERNAL_SERVER(500, "msg.internal.server"),
    PASSWORD_EXPIRED(40301, "msg.auth.password.expired"),
    ACCOUNT_LOCKED(423, "msg.auth.account.locked"),
    ACCOUNT_DISABLED(40302, "msg.auth.account.disabled"),
    INVALID_CREDENTIALS(40101, "msg.auth.invalid.credentials");

    private final int code;
    private final String description;

    ErrorApp(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public static ErrorApp getErrorApp(int code){
    	for(ErrorApp error : ErrorApp.values()){
    		if(error.getCode() == code){
    			return error;
    		}
    	}
    	return null;
    }
}
