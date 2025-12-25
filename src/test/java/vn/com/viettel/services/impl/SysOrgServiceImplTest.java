package vn.com.viettel.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

import vn.com.viettel.dto.SysOrgDto;
import vn.com.viettel.entities.SysOrg;
import vn.com.viettel.mapper.SysOrgMapper;
import vn.com.viettel.repositories.jpa.SysOrgRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Service: Quản lý đơn vị hệ thống (SYS_ORG)")
class SysOrgServiceImplTest {

    @Mock private SysOrgRepository repository;
    @Mock private SysOrgMapper mapper;
    @Mock private Translator translator;

    @InjectMocks private SysOrgServiceImpl service;

    @Captor private ArgumentCaptor<SysOrg> orgCaptor;

    private final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 12, 25, 10, 0);
    private SysOrgDto validDto;
    private SysOrg existingEntity;

    @BeforeEach
    void setUp() {
        validDto = new SysOrgDto();
        validDto.setOrgCode(" ORG001 "); // Test trim
        validDto.setOrgName(" Don Vi Test ");
        validDto.setIsActive(true);
        validDto.setCreatedBy(1L);
        validDto.setUpdatedBy(1L);

        existingEntity = new SysOrg();
        existingEntity.setId(100L);
        existingEntity.setOrgCode("ORG001");
        existingEntity.setOrgName("Don Vi Cu");
        existingEntity.setIsActive(false);
        existingEntity.setIsDeleted(false);
    }

    @Nested
    @DisplayName("Nghiệp vụ: Tạo mới đơn vị (create)")
    class CreateTests {

        @Test
        @DisplayName("Thành công - Kiểm tra trim dữ liệu và mặc định isActive")
        void create_Success_AndDefaultActive() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                validDto.setIsActive(null); // Trường hợp null
                when(repository.existsByOrgCodeIgnoreCaseAndIsDeletedFalse(anyString())).thenReturn(false);
                when(mapper.toEntity(any())).thenReturn(new SysOrg());
                when(repository.save(any())).thenReturn(existingEntity);

                service.create(validDto);

                verify(repository).save(orgCaptor.capture());
                SysOrg captured = orgCaptor.getValue();

                assertEquals("ORG001", captured.getOrgCode(), "Phải trim mã đơn vị");
                assertEquals("Don Vi Test", captured.getOrgName(), "Phải trim tên đơn vị");
                assertTrue(captured.getIsActive(), "Khi isActive null phải mặc định TRUE");
                assertEquals(FIXED_NOW, captured.getCreatedAt());
            }
        }

        @Test
        @DisplayName("Thất bại khi trùng mã đơn vị - Ném lỗi 409")
        void create_DuplicateCode_ThrowsException() {
            when(repository.existsByOrgCodeIgnoreCaseAndIsDeletedFalse("ORG001")).thenReturn(true);
            when(translator.getMessage(eq("org.code.duplicate"), any())).thenReturn("Mã đã tồn tại");

            assertThrows(CustomException.class, () -> service.create(validDto));
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Cập nhật đơn vị (update)")
    class UpdateTests {

        @Test
        @DisplayName("Thành công - Trường hợp đổi mã và mã mới chưa tồn tại")
        void update_CodeChanged_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                validDto.setOrgCode("NEW_CODE");
                when(repository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(existingEntity));
                when(repository.existsByOrgCodeIgnoreCaseAndIsDeletedFalse("NEW_CODE")).thenReturn(false);
                when(repository.save(any())).thenReturn(existingEntity);

                service.update(100L, validDto);

                verify(repository).save(orgCaptor.capture());
                assertEquals("NEW_CODE", orgCaptor.getValue().getOrgCode());
            }
        }

        @Test
        @DisplayName("Thành công - Giữ nguyên isActive nếu input null")
        void update_IsActiveNull_KeepOldValue() {
            validDto.setIsActive(null);
            existingEntity.setIsActive(true); // Giá trị cũ là true

            when(repository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);

            service.update(100L, validDto);

            verify(repository).save(orgCaptor.capture());
            assertTrue(orgCaptor.getValue().getIsActive(), "Phải giữ nguyên giá trị TRUE từ entity cũ");
        }

        @Test
        @DisplayName("Thất bại khi ID truyền vào là null - Ném lỗi 400")
        void update_IdNull_ThrowsException() {
            when(translator.getMessage("org.id.required")).thenReturn("ID không được để trống");
            CustomException ex = assertThrows(CustomException.class, () -> service.update(null, validDto));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Xóa đơn vị (delete)")
    class DeleteTests {

        @Test
        @DisplayName("Thành công - Xóa mềm và cập nhật thời gian")
        void delete_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                when(repository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(existingEntity));

                service.delete(100L);

                verify(repository).save(orgCaptor.capture());
                assertTrue(orgCaptor.getValue().getIsDeleted());
                assertEquals(FIXED_NOW, orgCaptor.getValue().getUpdatedAt());
            }
        }
    }

    @Nested
    @DisplayName("Kiểm tra Validation")
    class ValidationTests {
        @Test
        @DisplayName("Thất bại khi mã đơn vị chỉ chứa khoảng trắng")
        void validate_BlankCode_ThrowsException() {
            validDto.setOrgCode("   ");
            when(translator.getMessage("org.code.required")).thenReturn("Yêu cầu mã");
            assertThrows(CustomException.class, () -> service.create(validDto));
        }
    }
}