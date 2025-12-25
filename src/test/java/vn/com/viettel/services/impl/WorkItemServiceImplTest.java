package vn.com.viettel.services.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.entities.WorkItem;
import vn.com.viettel.mapper.WorkItemMapper;
import vn.com.viettel.repositories.jpa.ProjectItemRepository;
import vn.com.viettel.repositories.jpa.WorkItemRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: Dịch vụ Quản lý Công việc (WorkItemServiceImpl)")
class WorkItemServiceImplTest {

    @Mock
    private WorkItemRepository repository;

    @Mock
    private ProjectItemRepository projectItemRepository;

    @Mock
    private WorkItemMapper mapper;

    @Mock
    private Translator translator;

    @InjectMocks
    private WorkItemServiceImpl service;

    private MockedStatic<LocalDateTime> mockedLocalDateTime;
    private final LocalDateTime NOW = LocalDateTime.of(2025, 12, 25, 10, 0);

    @BeforeEach
    void setUpStatic() {
        mockedLocalDateTime = mockStatic(LocalDateTime.class);
        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(NOW);
    }

    @AfterEach
    void tearDownStatic() {
        mockedLocalDateTime.close();
    }

    @Nested
    @DisplayName("Nghiệp vụ: Tạo mới công việc (create)")
    class CreateTests {
        private final Long itemId = 1L;
        private WorkItemDto request;

        @BeforeEach
        void setup() {
            request = new WorkItemDto();
            request.setWorkItemName("Công việc A");
        }

        @Test
        @DisplayName("Lỗi: Ném 404 khi Project Item không tồn tại")
        void create_ProjectNotFound_ThrowsException() {
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(false);
            when(translator.getMessage(eq("project.item.notfound"), any())).thenReturn("Project not found");

            CustomException ex = assertThrows(CustomException.class, () -> service.create(itemId, request));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Lỗi: Ném 400 khi tên công việc bị trống hoặc null")
        void create_InvalidRequest_ThrowsException() {
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            request.setWorkItemName("");
            when(translator.getMessage("invalid.request")).thenReturn("Invalid");

            assertThrows(CustomException.class, () -> service.create(itemId, request));
        }

        @Test
        @DisplayName("Lỗi: Ném 409 khi tên công việc đã tồn tại trong cùng một ItemId")
        void create_DuplicateName_ThrowsException() {
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.existsByItemIdAndWorkItemNameAndIsDeletedFalse(itemId, request.getWorkItemName())).thenReturn(true);
            when(translator.getMessage(eq("workitem.duplicate"), any())).thenReturn("Duplicate");

            assertThrows(CustomException.class, () -> service.create(itemId, request));
        }

        @Test
        @DisplayName("Thành công: Kiểm tra gán đúng ItemId, IsDeleted=False và thời gian tạo")
        void create_HappyPath_Success() {
            // GIVEN
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.existsByItemIdAndWorkItemNameAndIsDeletedFalse(any(), any())).thenReturn(false);
            WorkItem entity = new WorkItem();
            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(any())).thenReturn(entity);

            // WHEN
            service.create(itemId, request);

            // THEN
            ArgumentCaptor<WorkItem> captor = ArgumentCaptor.forClass(WorkItem.class);
            verify(repository).save(captor.capture());
            WorkItem saved = captor.getValue();

            assertEquals(itemId, saved.getItemId());
            assertEquals(Boolean.FALSE, saved.getIsDeleted());
            assertEquals(NOW, saved.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Cập nhật công việc (update)")
    class UpdateTests {
        private final Long itemId = 1L;
        private final Long workItemId = 100L;
        private WorkItemDto request;
        private WorkItem existingEntity;

        @BeforeEach
        void setup() {
            request = new WorkItemDto();
            request.setWorkItemName("Tên mới");

            existingEntity = new WorkItem();
            existingEntity.setId(workItemId);
            existingEntity.setItemId(itemId);
        }

        @Test
        @DisplayName("Lỗi bảo mật: Ném 404 khi WorkItem tồn tại nhưng không thuộc về Project Item đang truy cập")
        void update_WrongItemId_ThrowsException() {
            existingEntity.setItemId(999L); // Khác với itemId = 1L truyền vào
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(workItemId)).thenReturn(Optional.of(existingEntity));
            when(translator.getMessage(eq("workitem.notfound"), any())).thenReturn("Not found");

            assertThrows(CustomException.class, () -> service.update(itemId, workItemId, request));
        }

        @Test
        @DisplayName("Thành công: Gọi mapper để cập nhật và set thời gian cập nhật mới")
        void update_HappyPath_Success() {
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(workItemId)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);

            service.update(itemId, workItemId, request);

            verify(mapper).updateEntity(existingEntity, request);
            assertEquals(NOW, existingEntity.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Xóa công việc (delete)")
    class DeleteTests {
        @Test
        @DisplayName("Thành công: Đánh dấu xóa mềm (isDeleted=True) và cập nhật thời gian")
        void delete_HappyPath_Success() {
            Long itemId = 1L;
            Long workItemId = 10L;
            WorkItem entity = new WorkItem();
            entity.setItemId(itemId);

            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(workItemId)).thenReturn(Optional.of(entity));

            service.delete(itemId, workItemId);

            ArgumentCaptor<WorkItem> captor = ArgumentCaptor.forClass(WorkItem.class);
            verify(repository).save(captor.capture());
            assertTrue(captor.getValue().getIsDeleted());
            assertEquals(NOW, captor.getValue().getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Truy vấn công việc (getById/getAll)")
    class ReadTests {
        @Test
        @DisplayName("getAll: Phải lọc theo đúng itemId và isDeleted=False")
        void getAll_Success() {
            Long itemId = 1L;
            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.findAllByItemIdAndIsDeletedFalse(itemId)).thenReturn(List.of(new WorkItem()));

            service.getAll(itemId);

            verify(repository).findAllByItemIdAndIsDeletedFalse(itemId);
        }

        @Test
        @DisplayName("getById: Thành công khi tìm thấy đúng ID và đúng ItemId")
        void getById_Success() {
            Long itemId = 1L;
            Long workItemId = 10L;
            WorkItem entity = new WorkItem();
            entity.setItemId(itemId);

            when(projectItemRepository.existsByIdAndIsDeletedFalse(itemId)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(workItemId)).thenReturn(Optional.of(entity));

            service.getById(itemId, workItemId);

            verify(mapper).toDto(entity);
        }
    }
}