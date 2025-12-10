package vn.com.viettel.core.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Jwt {
    Long exp;
    Long iat;
    String jti;
    String iss;
    String sub;
    String name;
    String azp;
    @JsonProperty("preferred_username")
    String preferredUsername;
    @JsonProperty("realm_access")
    JwtRoleRealm realmAccess;
    @JsonProperty("session_state")
    String sessionState;
}
