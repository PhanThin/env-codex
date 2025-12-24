package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingItemDto;
import vn.com.viettel.dto.OutstandingItemSearchRequestDto;

import java.util.List;

public interface OutstandingItemService {

    @Transactional
    OutstandingItemDto createOutstandingItem(OutstandingItemDto dto, MultipartFile[] acceptanceFiles, MultipartFile[] documents);

    @Transactional
    OutstandingItemDto updateOutstandingItem(Long id, OutstandingItemDto dto, MultipartFile[] acceptanceFiles, MultipartFile[] documents);

    @Transactional
    void deleteOutstandingItem(List<Long> ids);

    OutstandingItemDto getOutstandingItemById(Long id);

    @Transactional(readOnly = true)
    Page<OutstandingItemDto> searchOutstanding(OutstandingItemSearchRequestDto request);
}
