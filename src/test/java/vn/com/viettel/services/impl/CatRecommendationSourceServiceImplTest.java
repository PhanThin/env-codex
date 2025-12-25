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

import vn.com.viettel.dto.CatRecommendationSourceDto;
import vn.com.viettel.entities.CatRecommendationSource;
import vn.com.viettel.mapper.CatRecommendationSourceMapper;
import vn.com.viettel.repositories.jpa.CatRecommendationSourceRepository;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Service: Quản lý nguồn kiến nghị (CAT_RECOMMENDATION_SOURCE)")
class CatRecommendationSourceServiceImplTest {

    @Mock
    private CatRecommendationSourceRepository repository;

    @Mock
    private CatRecommendationSourceMapper mapper;

    @Mock
    private Translator translator;

    @InjectMocks
    private CatRecommendationSourceServiceImpl service;

    @Captor
    private ArgumentCaptor<CatRecommendationSource> entityCaptor;

    private final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 12, 25, 10, 0);
    private CatRecommendationSourceDto validDto;
    private CatRecommendationSource validEntity;

    @BeforeEach
    void setUp() {
        // Sử dụng Builder theo đúng Rule 5 của GĐ3
        validDto = CatRecommendationSourceDto.builder()
                .sourceCode("SRC001")
                .sourceName("Nguồn kiểm thử")
                .isActive("Y")
                .description("Mô tả kiểm thử")
                .build();

        validEntity = new CatRecommendationSource();
        validEntity.setId(1L);
        validEntity.setSourceCode("SRC001");
        validEntity.setSourceName("Nguồn kiểm thử");
        validEntity.setIsActive(true);
        validEntity.setIsDeleted(false);
    }

    @Nested
    @DisplayName("Nghiệp vụ: Tạo mới nguồn kiến nghị (create)")
    class CreateTests {

        @Test
        @DisplayName("Thất bại khi Request là null - Ném lỗi 400")
        void create_NullRequest_ThrowsException() {
            lenient().when(translator.getMessage("invalid.request")).thenReturn("Yêu cầu không hợp lệ");

            CustomException ex = assertThrows(CustomException.class, () -> service.create(null));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thất bại khi thiếu mã nguồn (sourceCode) - Ném lỗi 400")
        void create_BlankSourceCode_ThrowsException() {
            validDto.setSourceCode("");
            lenient().when(translator.getMessage("invalid.request")).thenReturn("Yêu cầu không hợp lệ");

            CustomException ex = assertThrows(CustomException.class, () -> service.create(validDto));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thành công khi dữ liệu hợp lệ - Kiểm tra gán giá trị mặc định")
        void create_ValidRequest_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                // GIVEN
                when(mapper.toEntity(any(CatRecommendationSourceDto.class))).thenReturn(new CatRecommendationSource());
                when(repository.save(any(CatRecommendationSource.class))).thenReturn(validEntity);
                when(mapper.toDto(any(CatRecommendationSource.class))).thenReturn(validDto);

                // WHEN
                CatRecommendationSourceDto result = service.create(validDto);

                // THEN
                assertNotNull(result);
                verify(repository).save(entityCaptor.capture());
                CatRecommendationSource captured = entityCaptor.getValue();

                // Kiểm tra logic "Paranoid": Đảm bảo flag deleted và ngày tạo luôn đúng
                assertEquals(Boolean.FALSE, captured.getIsDeleted(), "isDeleted phải luôn là FALSE khi tạo mới");
                assertEquals(FIXED_NOW, captured.getCreatedAt(), "createdAt phải trùng với thời điểm hệ thống");
            }
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Cập nhật nguồn kiến nghị (update)")
    class UpdateTests {

        @Test
        @DisplayName("Thất bại khi không tìm thấy ID - Ném lỗi 404")
        void update_NotFound_ThrowsException() {
            when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());
            when(translator.getMessage(eq("catRecommendationSource.notFound"), any())).thenReturn("Không tìm thấy");

            CustomException ex = assertThrows(CustomException.class, () -> service.update(1L, validDto));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Thành công khi ID tồn tại - Kiểm tra cập nhật ngày sửa")
        void update_ValidRequest_Success() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                // GIVEN
                when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(validEntity));
                doNothing().when(mapper).updateEntity(any(), any());
                when(repository.save(any())).thenReturn(validEntity);

                // WHEN
                service.update(1L, validDto);

                // THEN
                verify(mapper).updateEntity(validEntity, validDto);
                verify(repository).save(entityCaptor.capture());
                assertEquals(FIXED_NOW, entityCaptor.getValue().getUpdatedAt(), "updatedAt phải được cập nhật thời gian mới");
            }
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Truy vấn chi tiết (getById)")
    class GetByIdTests {

        @Test
        @DisplayName("Thất bại khi truy vấn ID không tồn tại hoặc đã bị xóa")
        void getById_NotFound_ThrowsException() {
            when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());
            when(translator.getMessage(anyString(), any())).thenReturn("Không tìm thấy");

            assertThrows(CustomException.class, () -> service.getById(1L));
        }

        @Test
        @DisplayName("Thành công khi truy vấn ID hợp lệ")
        void getById_Success() {
            when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(validEntity));
            when(mapper.toDto(validEntity)).thenReturn(validDto);

            CatRecommendationSourceDto result = service.getById(1L);

            assertNotNull(result);
            assertEquals(validDto.getSourceCode(), result.getSourceCode());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Truy vấn danh sách (getAll)")
    class GetAllTests {

        @Test
        @DisplayName("Thành công khi lấy toàn bộ danh sách chưa bị xóa")
        void getAll_ReturnsList() {
            when(repository.findAllByIsDeletedFalse()).thenReturn(List.of(validEntity));
            when(mapper.toDto(any())).thenReturn(validDto);

            List<CatRecommendationSourceDto> result = service.getAll();

            assertEquals(1, result.size());
            verify(mapper, times(1)).toDto(any());
        }

        @Test
        @DisplayName("Thành công khi danh sách rỗng - Trả về mảng rỗng thay vì null")
        void getAll_EmptyList_ReturnsEmpty() {
            when(repository.findAllByIsDeletedFalse()).thenReturn(Collections.emptyList());

            List<CatRecommendationSourceDto> result = service.getAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Nghiệp vụ: Xóa nguồn kiến nghị (delete)")
    class DeleteTests {

        @Test
        @DisplayName("Thất bại khi xóa ID không tồn tại")
        void delete_NotFound_ThrowsException() {
            when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());
            when(translator.getMessage(anyString(), any())).thenReturn("Không tìm thấy");

            assertThrows(CustomException.class, () -> service.delete(1L));
        }

        @Test
        @DisplayName("Thành công khi xóa mềm - Kiểm tra isDeleted thành TRUE")
        void delete_Success_SoftDelete() {
            try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_NOW);

                // GIVEN
                when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(validEntity));

                // WHEN
                service.delete(1L);

                // THEN
                verify(repository).save(entityCaptor.capture());
                CatRecommendationSource captured = entityCaptor.getValue();

                // Verification First Rule
                assertEquals(Boolean.TRUE, captured.getIsDeleted(), "Phải đánh dấu isDeleted = TRUE");
                assertEquals(FIXED_NOW, captured.getUpdatedAt(), "Phải cập nhật thời gian xóa vào updatedAt");
            }
        }
    }
}