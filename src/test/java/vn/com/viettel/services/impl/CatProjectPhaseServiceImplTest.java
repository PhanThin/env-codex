package vn.com.viettel.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.entities.CatProjectPhase;
import vn.com.viettel.mapper.CatProjectPhaseMapper;
import vn.com.viettel.repositories.jpa.CatProjectPhaseRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

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
    private LocalDateTime fixedTime;

    private final Long PROJECT_ID = 1L;
    private final Long PHASE_ID = 10L;
    private final String PHASE_CODE = "PHASE_01";

    @BeforeEach
    void setUp() {
        // Cố định thời gian hệ thống để kiểm chứng createdAt/updatedAt chính xác tuyệt đối
        fixedTime = LocalDateTime.of(2025, 12, 25, 10, 0);
        mockedLocalDateTime = mockStatic(LocalDateTime.class);
        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDateTime.close();
    }

    @Nested
    @DisplayName("Kịch bản kiểm thử: Phương thức tạo mới (create)")
    class CreateMethodTest {

        @Test
        @DisplayName("Thất bại: Ném ngoại lệ khi không tìm thấy thông tin dự án")
        void create_ProjectNotFound_ThrowsException() {
            // GIVEN
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(false);
            when(translator.getMessage(eq("project.notfound"), any())).thenReturn("Dự án không tồn tại");

            // WHEN & THEN
            CustomException ex = assertThrows(CustomException.class, () -> service.create(PROJECT_ID, new CatProjectPhaseDto()));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
            assertEquals("Dự án không tồn tại", ex.getMessage());
        }

        @Test
        @DisplayName("Thất bại: Ném ngoại lệ khi dữ liệu yêu cầu không hợp lệ (Mã giai đoạn trống)")
        void create_InvalidRequest_ThrowsException() {
            // GIVEN
            CatProjectPhaseDto dto = new CatProjectPhaseDto();
            dto.setPhaseCode(""); // Không hợp lệ
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(translator.getMessage("invalid.request")).thenReturn("Yêu cầu không hợp lệ");

            // WHEN & THEN
            CustomException ex = assertThrows(CustomException.class, () -> service.create(PROJECT_ID, dto));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thất bại: Ném ngoại lệ CONFLICT khi mã giai đoạn đã tồn tại trong dự án")
        void create_DuplicatePhaseCode_ThrowsException() {
            // GIVEN
            CatProjectPhaseDto dto = createValidDto();
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(repository.existsByProjectIdAndPhaseCodeAndIsDeletedFalse(PROJECT_ID, PHASE_CODE)).thenReturn(true);
            when(translator.getMessage(eq("project.phase.duplicate"), any())).thenReturn("Mã giai đoạn đã tồn tại");

            // WHEN & THEN
            CustomException ex = assertThrows(CustomException.class, () -> service.create(PROJECT_ID, dto));
            assertEquals(HttpStatus.CONFLICT.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thành công: Tạo mới giai đoạn khi dữ liệu đầu vào hợp lệ")
        void create_ValidData_Success() {
            // GIVEN
            CatProjectPhaseDto requestDto = createValidDto();
            CatProjectPhase entityBeforeSave = new CatProjectPhase();
            CatProjectPhase entityAfterSave = new CatProjectPhase();
            entityAfterSave.setId(PHASE_ID);

            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(repository.existsByProjectIdAndPhaseCodeAndIsDeletedFalse(PROJECT_ID, PHASE_CODE)).thenReturn(false);
            when(mapper.toEntity(requestDto)).thenReturn(entityBeforeSave);
            when(repository.save(any(CatProjectPhase.class))).thenReturn(entityAfterSave);
            when(mapper.toDto(entityAfterSave)).thenReturn(new CatProjectPhaseDto());

            // WHEN
            service.create(PROJECT_ID, requestDto);

            // THEN
            ArgumentCaptor<CatProjectPhase> captor = ArgumentCaptor.forClass(CatProjectPhase.class);
            verify(repository).save(captor.capture());
            CatProjectPhase savedEntity = captor.getValue();

            assertAll("Kiểm tra dữ liệu thực thể trước khi lưu",
                    () -> assertEquals(PROJECT_ID, savedEntity.getProjectId(), "ID dự án phải khớp"),
                    () -> assertEquals(Boolean.FALSE, savedEntity.getIsDeleted(), "Trạng thái xóa phải là FALSE"),
                    () -> assertEquals(fixedTime, savedEntity.getCreatedAt(), "Thời gian tạo phải khớp với thời gian hệ thống cố định")
            );
        }
    }

    @Nested
    @DisplayName("Kịch bản kiểm thử: Phương thức cập nhật (update)")
    class UpdateMethodTest {

        @Test
        @DisplayName("Thất bại: Ném ngoại lệ khi giai đoạn thuộc về một dự án khác")
        void update_WrongProject_ThrowsException() {
            // GIVEN
            CatProjectPhaseDto dto = createValidDto();
            CatProjectPhase existingEntity = new CatProjectPhase();
            existingEntity.setProjectId(999L); // ID dự án khác hoàn toàn

            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.of(existingEntity));
            when(translator.getMessage(eq("project.phase.notfound"), any())).thenReturn("Không tìm thấy giai đoạn");

            // WHEN & THEN
            assertThrows(CustomException.class, () -> service.update(PROJECT_ID, PHASE_ID, dto));
        }

        @Test
        @DisplayName("Thành công: Cập nhật thông tin và tự động ghi nhận thời gian chỉnh sửa")
        void update_ValidData_Success() {
            // GIVEN
            CatProjectPhaseDto dto = createValidDto();
            CatProjectPhase existingEntity = new CatProjectPhase();
            existingEntity.setProjectId(PROJECT_ID);

            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.of(existingEntity));

            // WHEN
            service.update(PROJECT_ID, PHASE_ID, dto);

            // THEN
            verify(mapper).updateEntity(existingEntity, dto);
            verify(repository).save(existingEntity);
            assertEquals(fixedTime, existingEntity.getUpdatedAt(), "Thời gian cập nhật phải khớp với thời gian hệ thống cố định");
        }
    }

    @Nested
    @DisplayName("Kịch bản kiểm thử: Phương thức xóa (delete)")
    class DeleteMethodTest {

        @Test
        @DisplayName("Thành công: Thực hiện xóa mềm (đổi trạng thái isDeleted sang TRUE)")
        void delete_Success() {
            // GIVEN
            CatProjectPhase existingEntity = new CatProjectPhase();
            existingEntity.setProjectId(PROJECT_ID);
            existingEntity.setIsDeleted(false);

            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(PHASE_ID)).thenReturn(Optional.of(existingEntity));

            // WHEN
            service.delete(PROJECT_ID, PHASE_ID);

            // THEN
            ArgumentCaptor<CatProjectPhase> captor = ArgumentCaptor.forClass(CatProjectPhase.class);
            verify(repository).save(captor.capture());
            CatProjectPhase deletedEntity = captor.getValue();

            assertTrue(deletedEntity.getIsDeleted(), "Trạng thái xóa phải được chuyển thành TRUE");
            assertEquals(fixedTime, deletedEntity.getUpdatedAt(), "Phải ghi nhận thời gian xóa vào trường updatedAt");
        }
    }

    @Nested
    @DisplayName("Kịch bản kiểm thử: Phương thức lấy danh sách (getAll)")
    class GetAllMethodTest {

        @Test
        @DisplayName("Thành công: Trả về danh sách DTO đã được sắp xếp theo thứ tự hiển thị")
        void getAll_Success() {
            // GIVEN
            CatProjectPhase entity = new CatProjectPhase();
            when(projectRepository.existsByIdAndIsDeletedFalse(PROJECT_ID)).thenReturn(true);
            when(repository.findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(PROJECT_ID))
                    .thenReturn(Collections.singletonList(entity));
            when(mapper.toDto(entity)).thenReturn(new CatProjectPhaseDto());

            // WHEN
            List<CatProjectPhaseDto> result = service.getAll(PROJECT_ID);

            // THEN
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(repository).findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(PROJECT_ID);
        }
    }

    // Phương thức hỗ trợ tạo dữ liệu mẫu
    private CatProjectPhaseDto createValidDto() {
        CatProjectPhaseDto dto = new CatProjectPhaseDto();
        dto.setPhaseCode(PHASE_CODE);
        dto.setPhaseName("Giai đoạn thiết kế");
        dto.setIsActive(true);
        return dto;
    }
}