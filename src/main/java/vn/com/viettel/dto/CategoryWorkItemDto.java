package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CategoryWorkItemDto {
    Long id;
    ProjectItemDto projectItem;
    CatProjectPhaseDto projectPhase;
    CatUnitDto unit;
    String categoryWorkItemCode;
    String categoryWorkItemName;
    UserDto updatedBy;
    LocalDateTime updatedAt;
    UserDto createdBy;
    LocalDateTime createdAt;
    Boolean isActive;
    String note;
    ProjectTypeDto projectType;
}
