package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.CatSurveyEquipmentDto;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

public interface CatSurveyEquipmentService {

    CatSurveyEquipmentDto create(CatSurveyEquipmentDto dto) throws CustomException;

    CatSurveyEquipmentDto update(Long id, CatSurveyEquipmentDto dto) throws CustomException;

    void delete(List<Long> equipmentIds) throws CustomException;

    Page<CatSurveyEquipmentDto> search(CatSurveyEquipmentSearchRequestDto request) throws CustomException;
}
