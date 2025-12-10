package vn.com.viettel.services;

import vn.com.viettel.core.dto.ChangeActionDTO;
import vn.com.viettel.entities.ChangeActionEntity;

public interface ChangeActionService {
    ChangeActionEntity saveChangeAction(ChangeActionDTO changeActionDTO);

    int saveChangeActionAndItems(ChangeActionDTO changeActionDTO, Object oldEntity, Object newEntity);
}
