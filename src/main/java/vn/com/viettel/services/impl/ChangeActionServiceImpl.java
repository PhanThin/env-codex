package vn.com.viettel.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.com.viettel.auth.utils.AuthUtils;
import vn.com.viettel.auth.utils.SecurityContextUtils;
import vn.com.viettel.core.dto.ChangeActionDTO;
import vn.com.viettel.entities.ChangeActionEntity;
import vn.com.viettel.repositories.jpa.ChangeActionRepositoryJPA;
import vn.com.viettel.services.ChangeActionItemService;
import vn.com.viettel.services.ChangeActionService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeActionServiceImpl implements ChangeActionService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChangeActionServiceImpl.class);
    private final ChangeActionRepositoryJPA changeActionRepositoryJPA;
    private final ChangeActionItemService changeActionItemService;
    @Override
    public ChangeActionEntity saveChangeAction(ChangeActionDTO changeActionDTO) {
        ChangeActionEntity changeActionEntity = new ChangeActionEntity();
        changeActionEntity.setChangeActionId(UUID.randomUUID());
        changeActionEntity.setTableName(changeActionDTO.getTableName());
        changeActionEntity.setAction(changeActionDTO.getAction());
        changeActionEntity.setCreateBy(SecurityContextUtils.getUsername() != null ? SecurityContextUtils.getUsername() : "SYSTEM");
        changeActionEntity.setCreateTime(System.currentTimeMillis());
        changeActionEntity.setStatus((short) 1);
        changeActionEntity.setItemId(changeActionDTO.getItemId());
        changeActionEntity.setDescription(changeActionDTO.getDescription());
        changeActionEntity.setIpAddress(AuthUtils.getClientIpAddress());
        changeActionEntity.setClientId(SecurityContextUtils.getUsername() != null ? SecurityContextUtils.getUsername() : "SYSTEM");
        changeActionEntity.setRealmName(SecurityContextUtils.getUsername() != null ? SecurityContextUtils.getUsername() : "SYSTEM");
        return changeActionRepositoryJPA.save(changeActionEntity);
    }

    @Override
    public int saveChangeActionAndItems(ChangeActionDTO changeActionDTO, Object oldEntity, Object newEntity) {
        try {
            ChangeActionEntity changeActionEntity = saveChangeAction(changeActionDTO);
            return changeActionItemService.saveChangeActionItems(changeActionEntity.getChangeActionId(), oldEntity, newEntity);
        } catch (Exception e) {
            LOGGER.error("Error saveChangeActionAndItems: ", e);
            return 0;
        }
    }
}
