package vn.com.viettel.mapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.dto.ProjectDto;
import vn.com.viettel.entities.CatProjectPhase;
import vn.com.viettel.entities.Project;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatProjectPhaseMapper {

    private final ModelMapper modelMapper;

    @PostConstruct
    private void configure() {

        // DTO -> Entity
        if (modelMapper.getTypeMap(CatProjectPhaseDto.class, CatProjectPhase.class) == null) {
            modelMapper.typeMap(CatProjectPhaseDto.class, CatProjectPhase.class)
                    .addMappings(mapper -> {
                        mapper.skip(CatProjectPhase::setId);
                        mapper.skip(CatProjectPhase::setProjectId);
                        mapper.skip(CatProjectPhase::setCreatedAt);
                        mapper.skip(CatProjectPhase::setCreatedBy);
                        mapper.skip(CatProjectPhase::setUpdatedAt);
                        mapper.skip(CatProjectPhase::setUpdatedBy);
                        mapper.skip(CatProjectPhase::setIsDeleted);
                    });
        }

        // Entity -> DTO
        if (modelMapper.getTypeMap(CatProjectPhase.class, CatProjectPhaseDto.class) == null) {
            modelMapper.typeMap(CatProjectPhase.class, CatProjectPhaseDto.class)
                    .addMappings(mapper -> {
                        mapper.skip(CatProjectPhaseDto::setProjectDto);
                    });
        }
    }
    public CatProjectPhaseDto toDto(
            CatProjectPhase entity,
            Map<Long, Project> projectMap
    ) {
        if (entity == null) return null;

        CatProjectPhaseDto dto = modelMapper.map(entity, CatProjectPhaseDto.class);

        ProjectDto projectDto;
        if (projectMap != null
                && entity.getProjectId() != null
                && projectMap.containsKey(entity.getProjectId())) {

            projectDto = modelMapper.map(projectMap.get(entity.getProjectId()), ProjectDto.class);

        } else {
            projectDto = new ProjectDto();
            projectDto.setId(entity.getProjectId());
        }
        dto.setProjectDto(projectDto);

        return dto;
    }
    public CatProjectPhase toEntity(CatProjectPhaseDto dto, Long projectId) {
        if (dto == null) return null;

        CatProjectPhase entity = modelMapper.map(dto, CatProjectPhase.class);
        entity.setProjectId(projectId);
        entity.setIsDeleted(Boolean.FALSE);

        return entity;
    }
    public void updateEntity(
            CatProjectPhaseDto dto,
            CatProjectPhase entity
    ) {
        if (dto == null || entity == null) return;

        modelMapper.map(dto, entity);
    }


}
