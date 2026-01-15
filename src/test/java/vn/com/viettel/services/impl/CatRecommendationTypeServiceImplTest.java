package vn.com.viettel.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.entities.CatRecommendationType;
import vn.com.viettel.mapper.CatRecommendationTypeMapper;
import vn.com.viettel.repositories.jpa.CatRecommendationTypeRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatRecommendationTypeServiceImplTest {

    @Mock
    private CatRecommendationTypeRepository repository;

    @Mock
    private CatRecommendationTypeMapper mapper;

    @Mock
    private Translator translator;

    @InjectMocks
    private CatRecommendationTypeServiceImpl service;

    private static final String MSG_INVALID = "Dữ liệu yêu cầu không hợp lệ";
    private static final String MSG_NOT_FOUND = "Loại khuyến nghị không tồn tại: {0}";

    // =========================================================
    // CREATE
    // =========================================================
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("TC_01: Create success - verify default & audit fields only")
        void create_success() {
            // GIVEN
            CatRecommendationTypeDto request = new CatRecommendationTypeDto();
            request.setTypeCode("ANY_CODE");
            request.setTypeName("ANY_NAME");
            request.setIsActive(true);

            CatRecommendationType mappedEntity = new CatRecommendationType();
            CatRecommendationType savedEntity = new CatRecommendationType();

            ArgumentCaptor<CatRecommendationType> captor =
                    ArgumentCaptor.forClass(CatRecommendationType.class);

            when(mapper.toEntity(request)).thenReturn(mappedEntity);
            when(repository.save(captor.capture())).thenReturn(savedEntity);
            when(mapper.toDto(savedEntity)).thenReturn(new CatRecommendationTypeDto());

            // WHEN
            CatRecommendationTypeDto result = service.create(request);

            // THEN
            assertNotNull(result);

            CatRecommendationType saved = captor.getValue();
            assertFalse(saved.getIsDeleted());
            assertNotNull(saved.getCreatedAt());
            assertNull(saved.getUpdatedAt());


            verify(mapper).toEntity(request);
            verify(repository).save(any());
            verify(mapper).toDto(savedEntity);
            verifyNoMoreInteractions(mapper, translator);
        }


        @Test
        @DisplayName("TC_02: Create fail - request null -> BAD_REQUEST")
        void create_fail_requestNull() {
            // GIVEN
            when(translator.getMessage("catRecommendationType.payload.null"))
                    .thenReturn(MSG_INVALID);

            // WHEN
            CustomException ex =
                    assertThrows(CustomException.class, () -> service.create(null));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
            assertEquals(MSG_INVALID, ex.getMessage());

            verify(translator).getMessage("catRecommendationType.payload.null");
            verifyNoInteractions(repository, mapper);
        }

    }

    // =========================================================
    // UPDATE
    // =========================================================
    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("TC_05: Update success - verify mutation & updatedAt")
        void update_success() {
            // GIVEN
            Long id = 10L;

            CatRecommendationTypeDto request = new CatRecommendationTypeDto();
            request.setTypeCode("ANY_CODE");
            request.setTypeName("ANY_NAME");
            request.setIsActive(true);

            CatRecommendationType existing = new CatRecommendationType();
            existing.setId(id);

            ArgumentCaptor<CatRecommendationType> captor =
                    ArgumentCaptor.forClass(CatRecommendationType.class);

            when(repository.findByIdAndIsDeletedFalse(id))
                    .thenReturn(Optional.of(existing));
            when(repository.save(captor.capture()))
                    .thenReturn(existing);

            // WHEN
            service.update(id, request);

            // THEN
            CatRecommendationType updated = captor.getValue();

            assertEquals(id, updated.getId());
            assertNotNull(updated.getUpdatedAt());

            verify(mapper).updateEntity(existing, request);
            verify(repository).save(existing);

            verifyNoInteractions(translator);
        }


        @Test
        @DisplayName("TC_06: Update fail - not found -> NOT_FOUND")
        void update_fail_notFound() {
            // GIVEN
            Long id = 999L;

            CatRecommendationTypeDto request = new CatRecommendationTypeDto();
            request.setTypeCode("ANY_CODE");
            request.setTypeName("ANY_NAME");

            when(repository.findByIdAndIsDeletedFalse(id))
                    .thenReturn(Optional.empty());

            when(translator.getMessage("catRecommendationType.notFound", id))
                    .thenReturn(MSG_NOT_FOUND);

            // WHEN
            CustomException ex =
                    assertThrows(CustomException.class, () -> service.update(id, request));

            // THEN
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
            assertEquals(MSG_NOT_FOUND, ex.getMessage());

            verify(translator)
                    .getMessage("catRecommendationType.notFound", id);
            verify(repository, never()).save(any());
            verifyNoInteractions(mapper);
        }

    }

    // =========================================================
    // DELETE
    // =========================================================
    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("TC_08: Soft delete success")
        void delete_success() {
            // GIVEN
            Long id = 1L;
            CatRecommendationType entity = new CatRecommendationType();
            entity.setIsDeleted(false);

            ArgumentCaptor<CatRecommendationType> captor =
                    ArgumentCaptor.forClass(CatRecommendationType.class);

            when(repository.findByIdAndIsDeletedFalse(id))
                    .thenReturn(Optional.of(entity));
            when(repository.save(captor.capture()))
                    .thenReturn(entity);

            // WHEN
            service.delete(id);

            // THEN
            CatRecommendationType deleted = captor.getValue();

            assertTrue(deleted.getIsDeleted());
            assertNotNull(deleted.getUpdatedAt());

            verify(repository).save(entity);
            verifyNoInteractions(mapper, translator);
        }
    }

    // =========================================================
    // READ
    // =========================================================
    @Nested
    @DisplayName("read()")
    class ReadTests {

        @Test
        @DisplayName("TC_10: Get by id success")
        void getById_success() {
            // GIVEN
            Long id = 1L;
            CatRecommendationType entity = new CatRecommendationType();
            CatRecommendationTypeDto dto = new CatRecommendationTypeDto();

            when(repository.findByIdAndIsDeletedFalse(id))
                    .thenReturn(Optional.of(entity));
            when(mapper.toDto(entity))
                    .thenReturn(dto);

            // WHEN
            CatRecommendationTypeDto result = service.getById(id);

            // THEN
            assertSame(dto, result);

            verify(mapper).toDto(entity);
            verifyNoMoreInteractions(translator);
        }

        @Test
        @DisplayName("TC_12: Get all success")
        void getAll_success() {
            // GIVEN
            CatRecommendationType e1 = new CatRecommendationType();
            CatRecommendationType e2 = new CatRecommendationType();

            CatRecommendationTypeDto d1 = new CatRecommendationTypeDto();
            CatRecommendationTypeDto d2 = new CatRecommendationTypeDto();

            when(repository.findAllByIsDeletedFalse())
                    .thenReturn(List.of(e1, e2));
            when(mapper.toDto(e1)).thenReturn(d1);
            when(mapper.toDto(e2)).thenReturn(d2);

            // WHEN
            List<CatRecommendationTypeDto> result = service.getAll();

            // THEN
            assertEquals(2, result.size());
            assertSame(d1, result.get(0));
            assertSame(d2, result.get(1));

            verifyNoInteractions(translator);
        }

        @Test
        @DisplayName("TC_13: Get all empty list")
        void getAll_empty() {
            // GIVEN
            when(repository.findAllByIsDeletedFalse())
                    .thenReturn(Collections.emptyList());

            // WHEN
            List<CatRecommendationTypeDto> result = service.getAll();

            // THEN
            assertNotNull(result);
            assertTrue(result.isEmpty());

            verifyNoInteractions(mapper, translator);
        }
    }
}
