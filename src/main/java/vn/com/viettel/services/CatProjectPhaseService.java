package vn.com.viettel.services;

import vn.com.viettel.dto.CatProjectPhaseDto;

import java.util.List;

public interface CatProjectPhaseService {

    CatProjectPhaseDto create(Long projectId, CatProjectPhaseDto request);

    CatProjectPhaseDto update(Long projectId, Long phaseId, CatProjectPhaseDto request);

    CatProjectPhaseDto getById(Long projectId, Long phaseId);

    List<CatProjectPhaseDto> getAll(Long projectId);

    void delete(Long projectId, Long phaseId);
}
