
package vn.com.viettel.services;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingAcceptanceDto;

import java.util.List;

/**
 * Service for OUTSTANDING_ACCEPTANCE.
 */
public interface OutstandingAcceptanceService {

    OutstandingAcceptanceDto create(Long outstandingId, OutstandingAcceptanceDto request, MultipartFile[] files);

    OutstandingAcceptanceDto update(Long outstandingId, Long acceptanceId, OutstandingAcceptanceDto request, MultipartFile[] files);

    OutstandingAcceptanceDto get(Long outstandingId, Long acceptanceId);

    void delete(Long outstandingId, Long acceptanceId);

    @Transactional(readOnly = true)
    List<OutstandingAcceptanceDto> getAll(Long outstandingId);
}
