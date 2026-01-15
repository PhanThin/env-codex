package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.CategoryWorkItemDto;
import vn.com.viettel.dto.CategoryWorkItemSearchRequest;

public interface CategoryWorkItemService {
    @Transactional(readOnly = true)
    Page<CategoryWorkItemDto> searchCategoryWorkItem(CategoryWorkItemSearchRequest request);

    @Transactional
    CategoryWorkItemDto createCategoryWorkItem(CategoryWorkItemDto dto);

    @Transactional
    CategoryWorkItemDto updateCategoryWorkItem(Long id, CategoryWorkItemDto dto);

    @Transactional
    void deleteCategoryWorkItems(java.util.List<Long> ids);

    @Transactional(readOnly = true)
    CategoryWorkItemDto getById(Long id);
}
