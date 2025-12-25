package vn.com.viettel.services.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.com.viettel.dto.SysUserDto;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.SysUserMapper;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: Dịch vụ quản lý người dùng (SysUserServiceImpl)")
class SysUserServiceImplTest {

    @Mock
    private SysUserRepository repository;

    @Mock
    private SysUserMapper mapper;

    @Mock
    private Translator translator;

    @InjectMocks
    private SysUserServiceImpl service;

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
    @DisplayName("Nghiệp vụ: Tạo mới người dùng (create)")
    class CreateTests {
        private SysUserDto inputDto;

        @BeforeEach
        void setup() {
            inputDto = new SysUserDto();
            inputDto.setUsername("  test.user  ");
            inputDto.setFullName("  Nguyen Van A  ");
            inputDto.setCreatedBy(1L);
            inputDto.setUpdatedBy(1L);
        }

        @Test
        @DisplayName("Case 1: Throw 400 khi Request Body (DTO) bị null")
        void create_DtoNull_ThrowsException() {
            when(translator.getMessage("user.request.null")).thenReturn("Request null");
            CustomException ex = assertThrows(CustomException.class, () -> service.create(null));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
            assertEquals("Request null", ex.getMessage());
        }

        @Test
        @DisplayName("Case 2: Throw 400 khi Username trống hoặc chỉ có khoảng trắng")
        void create_UsernameBlank_ThrowsException() {
            inputDto.setUsername("   ");
            when(translator.getMessage("user.username.required")).thenReturn("Username required");
            assertThrows(CustomException.class, () -> service.create(inputDto));
        }

        @Test
        @DisplayName("Case 3: Throw 409 khi Username đã tồn tại trong hệ thống (Case-insensitive)")
        void create_DuplicateUsername_ThrowsException() {
            when(repository.existsByUsernameIgnoreCaseAndIsDeletedFalse("test.user")).thenReturn(true);
            when(translator.getMessage(eq("user.username.duplicate"), any())).thenReturn("Duplicate");
            assertThrows(CustomException.class, () -> service.create(inputDto));
        }

        @Test
        @DisplayName("Case 4: Thành công - Kiểm tra logic Trim data, gán mặc định IsActive=True và Audit fields")
        void create_HappyPath_FullData() {
            inputDto.setIsActive(null); // Để test mặc định
            SysUser entity = new SysUser();
            when(repository.existsByUsernameIgnoreCaseAndIsDeletedFalse("test.user")).thenReturn(false);
            when(mapper.toEntity(inputDto)).thenReturn(entity);
            when(repository.save(any(SysUser.class))).thenReturn(entity);

            service.create(inputDto);

            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(repository).save(captor.capture());
            SysUser saved = captor.getValue();

            assertEquals("test.user", saved.getUsername(), "Username phải được trim");
            assertEquals("Nguyen Van A", saved.getFullName(), "FullName phải được trim");
            assertTrue(saved.getIsActive(), "Mặc định isActive phải là true khi input null");
            assertFalse(saved.getIsDeleted());
            assertEquals(NOW, saved.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Cập nhật người dùng (update)")
    class UpdateTests {
        private final Long userId = 100L;
        private SysUserDto updateDto;
        private SysUser existingEntity;

        @BeforeEach
        void setup() {
            updateDto = new SysUserDto();
            updateDto.setUsername("new.name");
            updateDto.setFullName("New Full Name");
            updateDto.setUpdatedBy(2L);

            existingEntity = new SysUser();
            existingEntity.setId(userId);
            existingEntity.setUsername("old.name");
            existingEntity.setIsActive(false);
        }

        @Test
        @DisplayName("Case 1: Throw 400 khi truyền userId là null")
        void update_IdNull_ThrowsException() {
            when(translator.getMessage("user.id.required")).thenReturn("ID req");
            assertThrows(CustomException.class, () -> service.update(null, updateDto));
        }

        @Test
        @DisplayName("Case 2: Throw 404 khi không tìm thấy người dùng hoặc đã bị xóa soft-delete")
        void update_UserNotFound_ThrowsException() {
            when(repository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());
            when(translator.getMessage(eq("user.notfound"), any())).thenReturn("Not found");
            assertThrows(CustomException.class, () -> service.update(userId, updateDto));
        }

        @Test
        @DisplayName("Case 3: Thành công - Giữ nguyên trạng thái isActive cũ nếu input DTO không truyền")
        void update_IsActiveNull_KeepOldStatus() {
            updateDto.setIsActive(null);
            when(repository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);

            service.update(userId, updateDto);

            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(repository).save(captor.capture());
            assertFalse(captor.getValue().getIsActive(), "Phải giữ trạng thái false từ entity cũ");
        }

        @Test
        @DisplayName("Case 4: Thành công - Cập nhật thời gian updatedAt và người thực hiện")
        void update_HappyPath_Success() {
            when(repository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(existingEntity));
            when(repository.save(any())).thenReturn(existingEntity);

            service.update(userId, updateDto);

            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(repository).save(captor.capture());
            assertEquals(NOW, captor.getValue().getUpdatedAt());
            assertEquals(2L, captor.getValue().getUpdatedBy());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Xóa người dùng (delete)")
    class DeleteTests {
        @Test
        @DisplayName("Case 1: Thành công - Thực hiện Soft-delete (isDeleted = true) và cập nhật thời gian")
        void delete_Success_SoftDelete() {
            Long userId = 1L;
            SysUser user = new SysUser();
            user.setIsDeleted(false);
            when(repository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

            service.delete(userId);

            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(repository).save(captor.capture());
            assertTrue(captor.getValue().getIsDeleted(), "isDeleted phải chuyển sang true");
            assertEquals(NOW, captor.getValue().getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Truy vấn người dùng (getById/getAll)")
    class GetAndReadTests {
        @Test
        @DisplayName("Case 1: getById thành công khi ID tồn tại")
        void getById_Success() {
            Long userId = 1L;
            SysUser entity = new SysUser();
            when(repository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(entity));
            when(mapper.toDto(entity)).thenReturn(new SysUserDto());

            assertNotNull(service.getById(userId));
        }

        @Test
        @DisplayName("Case 2: getAll chỉ lấy các bản ghi chưa bị xóa (isDeleted = false)")
        void getAll_Success() {
            when(repository.findAllByIsDeletedFalse()).thenReturn(List.of(new SysUser()));
            when(mapper.toDto(any())).thenReturn(new SysUserDto());

            List<SysUserDto> result = service.getAll();
            assertEquals(1, result.size());
            verify(repository).findAllByIsDeletedFalse();
        }
    }
}