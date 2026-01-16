package vn.com.viettel.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.viettel.dto.ProjectDto;
import vn.com.viettel.dto.ProjectItemDto;
import vn.com.viettel.entities.Project;
import vn.com.viettel.entities.ProjectItem;
import vn.com.viettel.mapper.ProjectItemMapper;
import vn.com.viettel.repositories.jpa.ProjectItemRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectItemServiceImpl - Unit Test CRUD PROJECT_ITEM")
class ProjectItemServiceImplTest {

    @InjectMocks
    private ProjectItemServiceImpl service;

    @Mock
    private ProjectItemRepository projectItemRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectItemMapper mapper;

    @Mock
    private Translator translator;

    private static final Long PROJECT_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final LocalDateTime FIXED_TIME =
            LocalDateTime.of(2025, 1, 1, 10, 0);

    private ProjectItemDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ProjectItemDto();
        validRequest.setItemCode("CODE");
        validRequest.setItemName("NAME");

        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(PROJECT_ID);
        validRequest.setProject(projectDto);
    }

    // =================================================================
    // CREATE
    // =================================================================
    @Nested
    @DisplayName("Create ProjectItem")
    class CreateTests {

        @Test
        @DisplayName("❌ Không tìm thấy Project → throw 404")
        void create_ProjectNotFound_ShouldThrow404() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(false);
            when(translator.getMessage("project.notfound", PROJECT_ID))
                    .thenReturn("PROJECT_NOT_FOUND");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.create(PROJECT_ID, validRequest)
            );

            assertEquals(404, ex.getCodeError());
            assertEquals("PROJECT_NOT_FOUND", ex.getMessage());
            verifyNoInteractions(projectItemRepository);
        }

        @Test
        @DisplayName("❌ Request null → throw 400")
        void create_RequestNull_ShouldThrow400() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);
            when(translator.getMessage("project.item.payload.null"))
                    .thenReturn("PAYLOAD_NULL");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.create(PROJECT_ID, null)
            );

            assertEquals(400, ex.getCodeError());
            assertEquals("PAYLOAD_NULL", ex.getMessage());
        }

        @Test
        @DisplayName("❌ ItemCode rỗng → throw 400")
        void create_ItemCodeBlank_ShouldThrow400() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);

            validRequest.setItemCode(" ");
            when(translator.getMessage("project.item.itemCode.required"))
                    .thenReturn("ITEM_CODE_REQUIRED");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.create(PROJECT_ID, validRequest)
            );

            assertEquals(400, ex.getCodeError());
            assertEquals("ITEM_CODE_REQUIRED", ex.getMessage());
        }

        @Test
        @DisplayName("❌ ItemName rỗng → throw 400")
        void create_ItemNameBlank_ShouldThrow400() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);

            validRequest.setItemName("");
            when(translator.getMessage("project.item.itemName.required"))
                    .thenReturn("ITEM_NAME_REQUIRED");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.create(PROJECT_ID, validRequest)
            );

            assertEquals(400, ex.getCodeError());
            assertEquals("ITEM_NAME_REQUIRED", ex.getMessage());
        }

        @Test
        @DisplayName("❌ Trùng ItemCode trong Project → throw 409")
        void create_Duplicate_ShouldThrow409() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);
            when(projectItemRepository
                    .existsByProjectIdAndItemCodeAndIsDeletedFalse(PROJECT_ID, "CODE"))
                    .thenReturn(true);
            when(translator.getMessage("project.item.duplicate", "CODE"))
                    .thenReturn("DUPLICATE");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.create(PROJECT_ID, validRequest)
            );

            assertEquals(409, ex.getCodeError());
            assertEquals("DUPLICATE", ex.getMessage());
            verify(projectItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("✅ Tạo mới ProjectItem thành công")
        void create_HappyPath_ShouldSaveCorrectEntity() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);
            when(projectItemRepository
                    .existsByProjectIdAndItemCodeAndIsDeletedFalse(PROJECT_ID, "CODE"))
                    .thenReturn(false);

            ProjectItem entity = new ProjectItem();
            entity.setProjectId(PROJECT_ID);
            entity.setIsActive(Boolean.TRUE);
            entity.setIsDeleted(Boolean.FALSE);
            when(mapper.toEntity(validRequest, PROJECT_ID)).thenReturn(entity);

            ProjectItem saved = new ProjectItem();
            when(projectItemRepository.save(any(ProjectItem.class)))
                    .thenReturn(saved);

            ProjectItemDto dto = new ProjectItemDto();
            when(mapper.toDto(saved, null, null)).thenReturn(dto);

            try (MockedStatic<LocalDateTime> mocked =
                         mockStatic(LocalDateTime.class)) {

                mocked.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                ProjectItemDto result =
                        service.create(PROJECT_ID, validRequest);

                ArgumentCaptor<ProjectItem> captor =
                        ArgumentCaptor.forClass(ProjectItem.class);
                verify(projectItemRepository).save(captor.capture());

                ProjectItem captured = captor.getValue();
                assertEquals(PROJECT_ID, captured.getProjectId());
                assertEquals(FIXED_TIME, captured.getCreatedAt());
                assertTrue(captured.getIsActive());
                assertFalse(captured.getIsDeleted());

                assertSame(dto, result);
                assertNull(result.getProject());
            }
        }
    }

    // =================================================================
    // UPDATE
    // =================================================================
    @Nested
    @DisplayName("Update ProjectItem")
    class UpdateTests {

        @Test
        @DisplayName("❌ Không tìm thấy Item → throw 404")
        void update_ItemNotFound_ShouldThrow404() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);
            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID))
                    .thenReturn(Optional.empty());
            when(translator.getMessage("project.item.notfound", ITEM_ID))
                    .thenReturn("ITEM_NOT_FOUND");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.update(PROJECT_ID, ITEM_ID, validRequest)
            );

            assertEquals(404, ex.getCodeError());
            assertEquals("ITEM_NOT_FOUND", ex.getMessage());
        }

        @Test
        @DisplayName("❌ Item không thuộc Project → throw 404")
        void update_ProjectMismatch_ShouldThrow404() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);

            ProjectItem entity = new ProjectItem();
            entity.setProjectId(999L);

            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID))
                    .thenReturn(Optional.of(entity));
            when(translator.getMessage("project.item.notfound", ITEM_ID))
                    .thenReturn("ITEM_NOT_FOUND");

            CustomException ex = assertThrows(
                    CustomException.class,
                    () -> service.update(PROJECT_ID, ITEM_ID, validRequest)
            );

            assertEquals(404, ex.getCodeError());
            assertEquals("ITEM_NOT_FOUND", ex.getMessage());
        }

        @Test
        @DisplayName("✅ Cập nhật ProjectItem thành công")
        void update_HappyPath_ShouldUpdateAndSave() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);

            ProjectItem entity = new ProjectItem();
            entity.setProjectId(PROJECT_ID);

            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID))
                    .thenReturn(Optional.of(entity));
            when(projectItemRepository.save(entity)).thenReturn(entity);
            when(mapper.toDto(entity, null, null)).thenReturn(new ProjectItemDto());

            try (MockedStatic<LocalDateTime> mocked =
                         mockStatic(LocalDateTime.class)) {

                mocked.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                service.update(PROJECT_ID, ITEM_ID, validRequest);

                verify(mapper).updateEntity(validRequest, entity);
                assertEquals(FIXED_TIME, entity.getUpdatedAt());
            }
        }
    }

    // =================================================================
    // GET BY ID
    // =================================================================
    @Nested
    @DisplayName("Get ProjectItem theo ID")
    class GetByIdTests {

        @Test
        @DisplayName("✅ Lấy ProjectItem theo ID thành công (có Project)")
        void getById_HappyPath_ShouldReturnDtoWithProject() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);

            ProjectItem entity = new ProjectItem();
            entity.setProjectId(PROJECT_ID);

            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID))
                    .thenReturn(Optional.of(entity));

            Project project = new Project();
            project.setId(PROJECT_ID);

            when(projectRepository.findAllById(List.of(PROJECT_ID)))
                    .thenReturn(List.of(project));

            ProjectItemDto dto = new ProjectItemDto();
            ProjectDto projectDto = new ProjectDto();
            projectDto.setId(PROJECT_ID);
            dto.setProject(projectDto);

            when(mapper.toDto(entity, Map.of(PROJECT_ID, project), null))
                    .thenReturn(dto);

            ProjectItemDto result =
                    service.getById(PROJECT_ID, ITEM_ID);

            assertNotNull(result.getProject());
            assertEquals(PROJECT_ID, result.getProject().getId());
        }
    }

    // =================================================================
    // GET ALL
    // =================================================================
    @Nested
    @DisplayName("Get danh sách ProjectItem")
    class GetAllTests {

        @Test
        @DisplayName("✅ Không có dữ liệu → trả về list rỗng")
        void getAll_Empty_ShouldReturnEmptyList() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);
            when(projectItemRepository.findAllByProjectIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(List.of());

            List<ProjectItemDto> result =
                    service.getAll(PROJECT_ID);

            assertTrue(result.isEmpty());
            verify(projectRepository, never()).findAllById(any());
        }
    }

    // =================================================================
    // DELETE
    // =================================================================
    @Nested
    @DisplayName("Delete (soft delete) ProjectItem")
    class DeleteTests {

        @Test
        @DisplayName("✅ Xóa mềm ProjectItem thành công")
        void delete_HappyPath_ShouldSoftDelete() {
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID))
                    .thenReturn(true);

            ProjectItem entity = new ProjectItem();
            entity.setProjectId(PROJECT_ID);

            when(projectItemRepository.findByIdAndIsDeletedFalse(ITEM_ID))
                    .thenReturn(Optional.of(entity));

            try (MockedStatic<LocalDateTime> mocked =
                         mockStatic(LocalDateTime.class)) {

                mocked.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                service.delete(PROJECT_ID, ITEM_ID);

                ArgumentCaptor<ProjectItem> captor =
                        ArgumentCaptor.forClass(ProjectItem.class);
                verify(projectItemRepository).save(captor.capture());

                ProjectItem saved = captor.getValue();
                assertTrue(saved.getIsDeleted());
                assertEquals(FIXED_TIME, saved.getUpdatedAt());
            }
        }
    }
}
