package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakRoleDTO {
    @JsonProperty("ID")
    String id;

    @JsonProperty("CLIENT_REALM_CONSTRAINT")
    String clientRealmConstraint;

    @JsonProperty("CLIENT_ROLE")
    boolean clientRole;

    @JsonProperty("DESCRIPTION")
    String description;

    @JsonProperty("NAME")
    String name;

    @JsonProperty("REALM_ID")
    String realmId;

    @JsonProperty("CLIENT")
    String client;

    @JsonProperty("REALM")
    String realm;
}
