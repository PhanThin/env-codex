package vn.com.viettel.services;

import vn.com.viettel.dto.KeycloakRoleDTO;

public interface KeycloakRoleService {
    void onRoleCreatedInKeycloak(KeycloakRoleDTO dto);
    void onRoleUpdatedInKeycloak(KeycloakRoleDTO dto);
    void onRoleDeletedInKeycloak(KeycloakRoleDTO dto);
}
