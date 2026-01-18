package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.CatScheduleAdjReasonDto;
import vn.com.viettel.dto.CatScheduleAdjReasonSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

public interface CatScheduleAdjReasonService {

    CatScheduleAdjReasonDto create(CatScheduleAdjReasonDto dto) throws CustomException;

    CatScheduleAdjReasonDto update(Long id, CatScheduleAdjReasonDto dto) throws CustomException;

    void delete(List<Long> reasonIds) throws CustomException;

    CatScheduleAdjReasonDto getDetail(Long id) throws CustomException;

    Page<CatScheduleAdjReasonDto> search(CatScheduleAdjReasonSearchRequestDto request) throws CustomException;
}
