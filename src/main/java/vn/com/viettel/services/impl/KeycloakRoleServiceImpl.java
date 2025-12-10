package vn.com.viettel.services.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.KeycloakRoleDTO;
import vn.com.viettel.services.KeycloakRoleService;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakRoleServiceImpl implements KeycloakRoleService {

    @Transactional
    @Override
    public void onRoleCreatedInKeycloak(KeycloakRoleDTO dto) {

    }

    @Transactional
    @Override
    public void onRoleUpdatedInKeycloak(KeycloakRoleDTO dto) {

    }

    @Transactional
    @Override
    public void onRoleDeletedInKeycloak(KeycloakRoleDTO dto) {

    }

}
