package vn.com.viettel.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.com.viettel.core.dto.CdcEvent;
import vn.com.viettel.dto.KeycloakRoleDTO;
import vn.com.viettel.services.KeycloakRoleService;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakRoleCDC {
    static final Logger LOGGER = LoggerFactory.getLogger(KeycloakRoleCDC.class);
    final KeycloakRoleService keycloakRoleService;

    @Bean
    public Consumer<CdcEvent<KeycloakRoleDTO>> roles() {
        return event -> {
            String eventName = event.isDeleteOp() ? event.getBefore().getName() : event.getAfter().getName();
            LOGGER.info("begin process KeycloakRoleCdcEvent: " + eventName);
            if (event.isCreateOp()) keycloakRoleService.onRoleCreatedInKeycloak(event.getAfter());
            else if (event.isUpdateOp()) keycloakRoleService.onRoleUpdatedInKeycloak(event.getAfter());
            else if (event.isDeleteOp()) keycloakRoleService.onRoleDeletedInKeycloak(event.getBefore());
            LOGGER.info("done process RealmCdcEvent: " + eventName);
        };
    }
}
