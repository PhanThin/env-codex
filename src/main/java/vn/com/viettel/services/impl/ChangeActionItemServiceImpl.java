package vn.com.viettel.services.impl;

import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.com.viettel.auth.utils.SecurityContextUtils;
import vn.com.viettel.entities.ChangeActionItemEntity;
import vn.com.viettel.repositories.jpa.ChangeActionItemRepositoryJPA;
import vn.com.viettel.services.ChangeActionItemService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeActionItemServiceImpl implements ChangeActionItemService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChangeActionItemServiceImpl.class);

    private final ChangeActionItemRepositoryJPA changeActionItemRepositoryJPA;

    @Override
    public int saveChangeActionItems(UUID changeActionId, Object oldEntity, Object newEntity) {
        if (newEntity == null) return 0;
        List<ChangeActionItemEntity> result = new ArrayList<>();
        try {
            String createBy = SecurityContextUtils.getUsername() != null ? SecurityContextUtils.getUsername() : "SYSTEM";
            Field[] fields;
            if (Objects.nonNull(oldEntity)) {
                fields = oldEntity.getClass().getDeclaredFields();
            } else {
                fields = newEntity.getClass().getDeclaredFields();
            }
            for (Field field : fields) {
                field.setAccessible(true);
                boolean change = true;
                if (Objects.nonNull(oldEntity)) change = !Objects.equals(field.get(oldEntity), field.get(newEntity));
                if (change) {
                    Column column = field.getAnnotation(Column.class);
                    if (Objects.nonNull(column)) {
                        ChangeActionItemEntity changeActionItemEntity = new ChangeActionItemEntity();
                        changeActionItemEntity.setChangeActionId(changeActionId);
                        changeActionItemEntity.setChangeActionItemId(UUID.randomUUID());
                        changeActionItemEntity.setField(field.getName());
                        if (Objects.nonNull(oldEntity)) changeActionItemEntity.setOldValue(getField(field, oldEntity));
                        changeActionItemEntity.setNewValue(getField(field, newEntity));
                        changeActionItemEntity.setCreateBy(createBy);
                        changeActionItemEntity.setCreateTime(System.currentTimeMillis());
                        changeActionItemEntity.setStatus((short) 1);
                        result.add(changeActionItemEntity);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(result)) {
                result = changeActionItemRepositoryJPA.saveAll(result);
            }
        } catch (Exception e) {
            LOGGER.error("Error saveChangeActionItems: ", e);
        }
        return result.size();
    }

    private String getField(Field field, Object entity) {
        String result = "";
        try {
            if (field != null) {
                result = Objects.toString(field.get(entity), null);
            }
        } catch (Exception e) {
            LOGGER.error("Error getField: ", e);
        }
        return result;
    }

}
