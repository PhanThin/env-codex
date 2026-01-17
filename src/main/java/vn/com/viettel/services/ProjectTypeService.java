package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.ProjectTypeDto;
import vn.com.viettel.dto.ProjectTypeSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

public interface ProjectTypeService {

    ProjectTypeDto create(ProjectTypeDto dto) throws CustomException;

    ProjectTypeDto update(Long id, ProjectTypeDto dto) throws CustomException;

    void delete(List<Long> projectTypeIds) throws CustomException;

    ProjectTypeDto getDetail(Long id) throws CustomException;

    Page<ProjectTypeDto> search(ProjectTypeSearchRequestDto request) throws CustomException;

    List<ProjectTypeDto> getAllProjectType();
}
