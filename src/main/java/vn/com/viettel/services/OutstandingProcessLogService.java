package vn.com.viettel.services;

import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingProcessLogDto;

import java.util.List;


/**
 * Service for OUTSTANDING_PROCESS_LOG CRUD APIs under an outstandingId.
 */
public interface OutstandingProcessLogService {

    OutstandingProcessLogDto create(Long outstandingId, OutstandingProcessLogDto request, MultipartFile[] attachment);

    OutstandingProcessLogDto update(Long outstandingId, Long processId, OutstandingProcessLogDto request, MultipartFile[] attachment);

    OutstandingProcessLogDto getById(Long outstandingId, Long processId);

    List<OutstandingProcessLogDto> getAll(Long outstandingId);

    void delete(Long outstandingId, Long processId);
}
