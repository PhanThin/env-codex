package vn.com.viettel.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import vn.com.viettel.dto.ProjectItemDto;
import vn.com.viettel.entities.ProjectItem;
import vn.com.viettel.mapper.ProjectItemMapper;
import vn.com.viettel.repositories.jpa.ProjectItemRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Service: Quản lý hạng mục dự án (PROJECT_ITEM)")
class ProjectItemServiceImplTest {

    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectItemMapper mapper;
    @Mock
    private Translator translator;

    @InjectMocks
    private ProjectItemServiceImpl service;

    @Captor
    private ArgumentCaptor<ProjectItem> itemCaptor;

    private final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 12, 25, 15, 0);
    private final Long PROJECT_ID = 100L;
    private final Long ITEM_ID = 200L;
    private ProjectItemDto validDto;
    private ProjectItem validEntity;

    @BeforeEach
    void setUp() {
        // Khởi tạo DTO (Do không có @Builder nên dùng setter)
        validDto = new ProjectItemDto();
        validDto.setItemCode("ITEM_001");
        validDto.setItemName("Hạng mục kiểm thử");

        // Khởi tạo Entity
        validEntity = new ProjectItem();
        validEntity.setId(ITEM_ID);
        validEntity.setProjectId(PROJECT_ID);
        validEntity.setItemCode("ITEM_001");
        validEntity.setItemName("Hạng mục kiểm thử");
        validEntity.setIsDeleted(false);
    }

    @Nested
    @DisplayName("Nghiệp vụ: Tạo mới hạng mục (create)")
    class CreateTests {

        @Test
        @DisplayName("Thất bại khi dự án không tồn tại - Ném lỗi 404")
        void create_ProjectNotFound_ThrowsException() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(false);
            when(translator.getMessage(eq("project.notfound"), any())).thenReturn("Dự án không tồn tại");

            CustomException ex = assertThrows(CustomException.class, () -> service.create(PROJECT_ID, validDto));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thất bại khi mã hạng mục đã tồn tại trong dự án - Ném lỗi 409")
        void create_DuplicateItemCode_ThrowsException() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(projectItemRepository.existsByProjectIdAndItemCodeAndIsDeletedFalse(PROJECT_ID, "ITEM_001")).thenReturn(true);
            when(translator.getMessage(eq("project.item.duplicate"), any())).thenReturn("Mã hạng mục đã tồn tại");

            CustomException ex = assertThrows(CustomException.class, () -> service.create(PROJECT_ID, validDto));
            assertEquals(HttpStatus.CONFLICT.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thành công - Kiểm tra gán đúng ProjectID và thời gian tạo")
        void create_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
                when(projectItemRepository.existsByProjectIdAndItemCodeAndIsDeletedFalse(anyLong(), anyString())).thenReturn(false);
                when(mapper.toEntity(any())).thenReturn(new ProjectItem());
                when(projectItemRepository.save(any())).thenReturn(validEntity);
                when(mapper.toDto(any())).thenReturn(validDto);

                service.create(PROJECT_ID, validDto);

                verify(projectItemRepository).save(itemCaptor.capture());
                ProjectItem captured = itemCaptor.getValue();
                assertEquals(PROJECT_ID, captured.getProjectId());
                assertEquals(Boolean.FALSE, captured.getIsDeleted());
                assertEquals(FIXED_NOW, captured.getCreatedAt());
            }
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Cập nhật hạng mục (update)")
    class UpdateTests {

        @Test
        @DisplayName("Thất bại khi hạng mục thuộc dự án khác (Bảo mật) - Ném lỗi 404")
        void update_ProjectIdMismatch_ThrowsException() {
            ProjectItem differentProjectItem = new ProjectItem();
            differentProjectItem.setProjectId(999L); // Khác với PROJECT_ID truyền vào

            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID)).thenReturn(Optional.of(differentProjectItem));
            when(translator.getMessage(anyString(), any())).thenReturn("Không tìm thấy hạng mục");

            CustomException ex = assertThrows(CustomException.class, () -> service.update(PROJECT_ID, ITEM_ID, validDto));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thành công - Kiểm tra gọi Mapper và cập nhật updatedAt")
        void update_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
                when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID)).thenReturn(Optional.of(validEntity));
                when(projectItemRepository.save(any())).thenReturn(validEntity);

                service.update(PROJECT_ID, ITEM_ID, validDto);

                verify(mapper).updateEntity(validEntity, validDto);
                verify(projectItemRepository).save(itemCaptor.capture());
                assertEquals(FIXED_NOW, itemCaptor.getValue().getUpdatedAt());
            }
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Xóa hạng mục (delete)")
    class DeleteTests {

        @Test
        @DisplayName("Thành công - Kiểm tra Soft Delete")
        void delete_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
                when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID)).thenReturn(Optional.of(validEntity));

                service.delete(PROJECT_ID, ITEM_ID);

                verify(projectItemRepository).save(itemCaptor.capture());
                ProjectItem captured = itemCaptor.getValue();
                assertTrue(captured.getIsDeleted());
                assertEquals(FIXED_NOW, captured.getUpdatedAt());
            }
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Truy vấn (get/list)")
    class QueryTests {

        @Test
        @DisplayName("Lấy danh sách hạng mục theo Project ID")
        void getAll_Success() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(projectItemRepository.findAllByProjectIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(List.of(validEntity));
            when(mapper.toDto(any())).thenReturn(validDto);

            List<ProjectItemDto> result = service.getAll(PROJECT_ID);

            assertEquals(1, result.size());
            verify(projectItemRepository).findAllByProjectIdAndIsDeletedFalse(PROJECT_ID);
        }

        @Test
        @DisplayName("Lấy chi tiết hạng mục thành công")
        void getById_Success() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID)).thenReturn(Optional.of(validEntity));
            when(mapper.toDto(any())).thenReturn(validDto);

            ProjectItemDto result = service.getById(PROJECT_ID, ITEM_ID);

            assertNotNull(result);
            verify(mapper).toDto(validEntity);
        }
    }

    @Nested
    @DisplayName("Kiểm tra tính hợp lệ dữ liệu (validateRequest)")
    class ValidationTests {

        @Test
        @DisplayName("Thất bại khi DTO null")
        void validate_NullDto_ThrowsException() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(translator.getMessage(anyString())).thenReturn("Lỗi");

            assertThrows(CustomException.class, () -> service.create(PROJECT_ID, null));
        }

        @Test
        @DisplayName("Thất bại khi itemCode rỗng")
        void validate_EmptyCode_ThrowsException() {
            validDto.setItemCode("");
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);

            assertThrows(CustomException.class, () -> service.create(PROJECT_ID, validDto));
        }
    }
}