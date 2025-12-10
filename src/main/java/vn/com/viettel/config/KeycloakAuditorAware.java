package vn.com.viettel.config;

import org.jetbrains.annotations.NotNull;
import org.keycloak.KeycloakPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class KeycloakAuditorAware implements AuditorAware<String> {

    @NotNull
    public Optional<String> getCurrentAuditor() {
        KeycloakPrincipal<?> principal =
                (KeycloakPrincipal<?>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String userId = principal.getKeycloakSecurityContext().getToken().getPreferredUsername();
        return Optional.ofNullable(userId);
    }
}