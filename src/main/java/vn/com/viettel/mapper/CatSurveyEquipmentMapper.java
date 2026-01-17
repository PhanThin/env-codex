package vn.com.viettel.mapper;

import org.mapstruct.*;
import vn.com.viettel.dto.CatSurveyEquipmentDto;
import vn.com.viettel.entities.CatSurveyEquipment;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatSurveyEquipmentMapper {

    CatSurveyEquipmentDto toDto(CatSurveyEquipment entity);

    @Mapping(target = "equipmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    CatSurveyEquipment toEntity(CatSurveyEquipmentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "equipmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromDto(CatSurveyEquipmentDto dto, @MappingTarget CatSurveyEquipment entity);

    @AfterMapping
    default void trimStrings(@MappingTarget CatSurveyEquipment entity) {
        if (entity.getEquipmentCode() != null) {
            entity.setEquipmentCode(entity.getEquipmentCode().trim());
        }
        if (entity.getEquipmentName() != null) {
            entity.setEquipmentName(entity.getEquipmentName().trim());
        }
        if (entity.getModelCode() != null) {
            entity.setModelCode(entity.getModelCode().trim());
        }
        if (entity.getManufacturerName() != null) {
            entity.setManufacturerName(entity.getManufacturerName().trim());
        }
        if (entity.getUomName() != null) {
            entity.setUomName(entity.getUomName().trim());
        }
        if (entity.getManageUnitName() != null) {
            entity.setManageUnitName(entity.getManageUnitName().trim());
        }
        if (entity.getNote() != null) {
            entity.setNote(entity.getNote().trim());
        }
        if (entity.getIsActive() != null) {
            entity.setIsActive(entity.getIsActive().trim().toUpperCase());
        }
        if (entity.getIsDeleted() != null) {
            entity.setIsDeleted(entity.getIsDeleted().trim().toUpperCase());
        }
    }
}
