package vn.com.viettel.services;

import java.util.UUID;

public interface ChangeActionItemService {
    int saveChangeActionItems(UUID changeActionId, Object oldEntity, Object newEntity);
}
