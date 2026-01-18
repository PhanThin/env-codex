package vn.com.viettel.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import vn.com.viettel.dto.CatScheduleAdjReasonDto;
import vn.com.viettel.entities.CatScheduleAdjReason;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatScheduleAdjReasonMapper {

    @Mapping(target = "reasonId", source = "reasonId")
    CatScheduleAdjReasonDto toDto(CatScheduleAdjReason entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "reasonCode", source = "reasonCode")
    @Mapping(target = "reasonName", source = "reasonName")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "isActive", source = "isActive")
    CatScheduleAdjReason toEntity(CatScheduleAdjReasonDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "reasonCode", source = "reasonCode")
    @Mapping(target = "reasonName", source = "reasonName")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "isActive", source = "isActive")
    void updateEntityFromDto(CatScheduleAdjReasonDto dto, @MappingTarget CatScheduleAdjReason entity);
}
