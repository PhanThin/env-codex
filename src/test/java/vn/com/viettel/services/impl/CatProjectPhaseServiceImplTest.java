package vn.com.viettel.services.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.entities.CatProjectPhase;
import vn.com.viettel.entities.Project;
import vn.com.viettel.mapper.CatProjectPhaseMapper;
import vn.com.viettel.repositories.jpa.CatProjectPhaseRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử dịch vụ quản lý giai đoạn dự án (CatProjectPhaseService)")
class CatProjectPhaseServiceImplTest {

    @Mock
    private CatProjectPhaseRepository repository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CatProjectPhaseMapper mapper;
    @Mock
    private Translator translator;

    @InjectMocks
    private CatProjectPhaseServiceImpl service;

    private MockedStatic<LocalDateTime> mockedLocalDateTime;
    private LocalDateTime fixedNow;

    private final Long PROJECT_ID = 1L;
    private final Long PHASE_ID = 10L;

    @BeforeEach
    void setUp() {
        fixedNow = LocalDateTime.of(2025, 12, 26, 10, 0);
        // Thay đổi dòng dưới đây:
        mockedLocalDateTime = mockStatic(LocalDateTime.class);
        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDateTime.close();
    }

    private void mockProjectExists(boolean exists) {
        when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(exists);
        if (!exists) {
            when(translator.getMessage(eq("project.notfound"), any())).thenReturn("Không tìm thấy dự án");
        }
    }

    @Nested
    @DisplayName("Nhóm kiểm thử Validation (Ràng buộc dữ liệu)")
    class ValidationTests {
        @Test
        @DisplayName("Kiểm tra Project ID không tồn tại - Phải ném lỗi 404")
        void validateProject_NotFound_ShouldThrowException() {
            mockProjectExists(false);

            CustomException ex = assertThrows(CustomException.class, () -> service.getById(PROJECT_ID, PHASE_ID));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Kiểm tra Request null - Phải ném lỗi 400")
        void validateRequest_NullPayload_ShouldThrowException() {
            mockProjectExists(true);
            when(translator.getMessage("project.phase.payload.null")).thenReturn("Dữ liệu trống");

            assertThrows(CustomException.class, () -> service.create(PROJECT_ID, null));
        }

        @Test
        @DisplayName("Kiểm tra thiếu Phase Code - Phải ném lỗi 400")
        void validateRequest_EmptyPhaseCode_ShouldThrowException() {
            mockProjectExists(true);
            CatProjectPhaseDto dto = new CatProjectPhaseDto();
            dto.setPhaseCode("");
            when(translator.getMessage("project.phase.phaseCode.required")).thenReturn("Mã giai đoạn bắt buộc");

            assertThrows(CustomException.class, () -> service.create(PROJECT_ID, dto));
        }
    }

    @Nested
    @DisplayName("Nhóm kiểm thử chức năng Thêm mới (Create)")
    class CreateTests {
        @Test
        @DisplayName("Trùng mã giai đoạn trong dự án - Phải ném lỗi 409")
        void create_DuplicatePhaseCode_ShouldThrowException() {
            mockProjectExists(true);
            CatProjectPhaseDto request = createValidDto();
            when(repository.existsByProjectIdAndPhaseCodeAndIsDeletedFalse(PROJECT_ID, "P01")).thenReturn(true);
            when(translator.getMessage(eq("project.phase.duplicate"), any())).thenReturn("Mã giai đoạn đã tồn tại");

            assertThrows(CustomException.class, () -> service.create(PROJECT_ID, request));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Thêm mới thành công - Phải lưu đúng thời gian tạo và thông tin")
        void create_Success_ShouldReturnDto() {
            mockProjectExists(true);
            CatProjectPhaseDto request = createValidDto();
            CatProjectPhase entity = new CatProjectPhase();
            CatProjectPhase savedEntity = new CatProjectPhase();
            savedEntity.setProjectId(PROJECT_ID);

            when(repository.existsByProjectIdAndPhaseCodeAndIsDeletedFalse(PROJECT_ID, "P01")).thenReturn(false);
            when(mapper.toEntity(request, PROJECT_ID)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(projectRepository.findByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(new Project()));

            service.create(PROJECT_ID, request);

            ArgumentCaptor<CatProjectPhase> captor = ArgumentCaptor.forClass(CatProjectPhase.class);
            verify(repository).save(captor.capture());
            assertEquals(fixedNow, captor.getValue().getCreatedAt(), "Thời gian tạo phải được gán bằng LocalDateTime.now()");
        }
    }

    @Nested
    @DisplayName("Nhóm kiểm thử chức năng Cập nhật (Update)")
    class UpdateTests {
        @Test
        @DisplayName("Cập nhật giai đoạn không tồn tại - Phải ném lỗi 404")
        void update_PhaseNotFound_ShouldThrowException() {
            mockProjectExists(true);
            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.empty());
            when(translator.getMessage(eq("project.phase.notfound"), any())).thenReturn("Không thấy giai đoạn");

            assertThrows(CustomException.class, () -> service.update(PROJECT_ID, PHASE_ID, createValidDto()));
        }

        @Test
        @DisplayName("Giai đoạn tồn tại nhưng không thuộc dự án chỉ định - Phải ném lỗi 404 (Bảo mật)")
        void update_WrongProjectId_ShouldThrowException() {
            mockProjectExists(true);
            CatProjectPhase existing = new CatProjectPhase();
            existing.setProjectId(999L);
            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.of(existing));
            when(translator.getMessage(eq("project.phase.notfound"), any())).thenReturn("Không thấy giai đoạn");

            assertThrows(CustomException.class, () -> service.update(PROJECT_ID, PHASE_ID, createValidDto()));
        }

        @Test
        @DisplayName("Cập nhật thành công - Phải cập nhật thời gianUpdatedAt")
        void update_Success_ShouldUpdateTimestamp() {
            mockProjectExists(true);
            CatProjectPhase existing = new CatProjectPhase();
            existing.setProjectId(PROJECT_ID);

            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.of(existing));
            when(projectRepository.findByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(new Project()));

            service.update(PROJECT_ID, PHASE_ID, createValidDto());

            ArgumentCaptor<CatProjectPhase> captor = ArgumentCaptor.forClass(CatProjectPhase.class);
            verify(repository).save(captor.capture());
            assertEquals(fixedNow, captor.getValue().getUpdatedAt(), "Thời gian cập nhật phải được làm mới");
        }
    }

    @Nested
    @DisplayName("Nhóm kiểm thử chức năng Xóa (Delete)")
    class DeleteTests {
        @Test
        @DisplayName("Xóa logic thành công - Phải set isDeleted = TRUE")
        void delete_Success_ShouldSetDeletedTrue() {
            mockProjectExists(true);
            CatProjectPhase existing = new CatProjectPhase();
            existing.setProjectId(PROJECT_ID);
            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.of(existing));

            service.delete(PROJECT_ID, PHASE_ID);

            ArgumentCaptor<CatProjectPhase> captor = ArgumentCaptor.forClass(CatProjectPhase.class);
            verify(repository).save(captor.capture());
            assertTrue(captor.getValue().getIsDeleted(), "Cờ xóa phải được set thành TRUE");
            assertEquals(fixedNow, captor.getValue().getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Nhóm kiểm thử chức năng Truy vấn (Query)")
    class QueryTests {
        @Test
        @DisplayName("Lấy danh sách tất cả giai đoạn - Phải map đúng Project tương ứng")
        void getAll_Success_ShouldMapCorrectly() {
            mockProjectExists(true);
            CatProjectPhase phase = new CatProjectPhase();
            phase.setProjectId(PROJECT_ID);

            when(repository.findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(PROJECT_ID))
                    .thenReturn(List.of(phase));
            when(projectRepository.findByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(new Project()));

            service.getAll(PROJECT_ID);

            verify(mapper).toDto(eq(phase), argThat(map -> map.containsKey(PROJECT_ID)));
        }
    }

    private CatProjectPhaseDto createValidDto() {
        CatProjectPhaseDto dto = new CatProjectPhaseDto();
        dto.setPhaseCode("P01");
        dto.setPhaseName("Giai đoạn 1");
        return dto;
    }
}