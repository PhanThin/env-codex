//package vn.com.viettel.services.impl;
//
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import vn.com.viettel.dto.OutstandingTypeDto;
//import vn.com.viettel.dto.OutstandingTypeDto;
//import vn.com.viettel.entities.CatOutstandingType;
//import vn.com.viettel.mapper.CatOutstandingTypeMapper;
//import vn.com.viettel.repositories.jpa.CatOutstandingTypeRepository;
//import vn.com.viettel.utils.Translator;
//import vn.com.viettel.utils.exceptions.CustomException;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit Test for CatOutstandingTypeServiceImpl.
// * Standards: Perfectionism, Exhaustive Testing, No any() for side-effects.
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("Kiểm thử dịch vụ CatOutstandingTypeService")
//class CatOutstandingTypeServiceImplTest {
//
//    @Mock
//    private CatOutstandingTypeRepository repository;
//
//    @Mock
//    private CatOutstandingTypeMapper mapper;
//
//    @Mock
//    private Translator translator;
//
//    @InjectMocks
//    private CatOutstandingTypeServiceImpl service;
//
//    private MockedStatic<LocalDateTime> mockedLocalDateTime;
//    // Thời gian cố định để verify chính xác tuyệt đối các trường Audit
//    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 12, 24, 10, 0, 0);
//
//    @BeforeEach
//    void setUp() {
//        mockedLocalDateTime = mockStatic(LocalDateTime.class);
//        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime);
//    }
//
//    @AfterEach
//    void tearDown() {
//        mockedLocalDateTime.close();
//    }
//
//    // =================================================================
//    // NHÓM TEST TẠO MỚI (CREATE)
//    // =================================================================
//    @Nested
//    @DisplayName("create(): Tạo mới")
//    class CreateTests {
//
//        @Test
//        @DisplayName("TC_01: Tạo mới thành công - Xác minh logic Entity và Audit Fields")
//        void create_success() {
//            // GIVEN
//            OutstandingTypeDto request = OutstandingTypeDto.builder()
//                    .typeCode("OT_001")
//                    .typeName("Loại nợ 1")
//                    .isActive(true)
//                    .build();
//
//            CatOutstandingType entityFromMapper = new CatOutstandingType();
//            entityFromMapper.setTypeCode("OT_001");
//            entityFromMapper.setTypeName("Loại nợ 1");
//            entityFromMapper.setIsActive(true);
//
//            when(mapper.toEntity(request)).thenReturn(entityFromMapper);
//            // RULE 3: Stubbing chính xác instance, KHÔNG dùng any()
//            when(repository.save(entityFromMapper)).thenReturn(entityFromMapper);
//            when(mapper.toDto(entityFromMapper)).thenReturn(request);
//
//            // WHEN
//            OutstandingTypeDto result = service.create(request);
//
//            // THEN
//            ArgumentCaptor<CatOutstandingType> captor = ArgumentCaptor.forClass(CatOutstandingType.class);
//            verify(repository).save(captor.capture());
//            CatOutstandingType saved = captor.getValue();
//
//            assertAll("Xác minh chi tiết Entity trước khi persist",
//                    () -> assertEquals("OT_001", saved.getTypeCode()),
//                    () -> assertEquals("Loại nợ 1", saved.getTypeName()),
//                    () -> assertEquals(Boolean.FALSE, saved.getIsDeleted(), "Mặc định isDeleted phải là FALSE"),
//                    () -> assertEquals(fixedTime, saved.getCreatedAt(), "CreatedAt phải khớp với thời điểm mock"),
//                    () -> assertNull(saved.getUpdatedAt(), "Mới tạo thì UpdatedAt phải là null")
//            );
//            assertNotNull(result);
//            verify(repository, times(1)).save(entityFromMapper);
//        }
//        @Test
//        @DisplayName("TC_01.1: Tạo mới thành công - TYPE_CODE = null")
//        void create_success_typeCode_null() {
//            // GIVEN
//            OutstandingTypeDto request = OutstandingTypeDto.builder()
//                    .typeCode(null)
//                    .isActive(true)
//                    .typeName("Loại nợ không mã")
//                    .build();
//
//            CatOutstandingType entityFromMapper = new CatOutstandingType();
//            entityFromMapper.setTypeCode(null);
//            entityFromMapper.setTypeName("Loại nợ không mã");
//            entityFromMapper.setIsActive(true);
//
//
//            when(mapper.toEntity(request)).thenReturn(entityFromMapper);
//            when(repository.save(entityFromMapper)).thenReturn(entityFromMapper);
//            when(mapper.toDto(entityFromMapper)).thenReturn(request);
//
//            // WHEN
//            OutstandingTypeDto result = service.create(request);
//
//            // THEN
//            ArgumentCaptor<CatOutstandingType> captor = ArgumentCaptor.forClass(CatOutstandingType.class);
//            verify(repository).save(captor.capture());
//
//            CatOutstandingType saved = captor.getValue();
//            assertNull(saved.getTypeCode(), "TYPE_CODE phải được phép null");
//            assertEquals("Loại nợ không mã", saved.getTypeName());
//
//            assertNotNull(result);
//        }
//
//        @Test
//        @DisplayName("TC_02: Tạo mới thất bại - Thiếu TYPE_NAME (bắt buộc)")
//        void create_fail_missing_typeName() {
//            // GIVEN
//            OutstandingTypeDto request = OutstandingTypeDto.builder()
//                    .typeCode(null) // hợp lệ
//                    .typeName("")   // KHÔNG hợp lệ
//                    .build();
//
//            when(translator.getMessage("catOutstandingType.typeName.required"))
//                    .thenReturn("TYPE_NAME không được để trống");
//
//            // WHEN & THEN
//            CustomException ex = assertThrows(CustomException.class, () -> service.create(request));
//
//            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
//            assertEquals("TYPE_NAME không được để trống", ex.getMessage());
//
//            verify(repository, never()).save(any());
//        }
//
//    }
//
//    // =================================================================
//    // NHÓM TEST CẬP NHẬT (UPDATE)
//    // =================================================================
//    @Nested
//    @DisplayName("update(): Cập nhật thông tin CatOutstandingType")
//    class UpdateTests {
//
//        @Test
//        @DisplayName("TC_03: Cập nhật thành công - Xác minh cập nhật UpdatedAt")
//        void update_success() {
//            // GIVEN
//            Long id = 100L;
//            OutstandingTypeDto request = OutstandingTypeDto.builder()
//                    .typeCode("CODE_NEW")
//                    .typeName("NAME_NEW")
//                    .isActive(true)
//                    .build();
//
//            CatOutstandingType existingEntity = new CatOutstandingType();
//            existingEntity.setId(id);
//            existingEntity.setIsDeleted(false);
//            existingEntity.setIsActive(true);
//
//            when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(existingEntity));
//            when(repository.save(existingEntity)).thenReturn(existingEntity);
//
//            // WHEN
//            service.update(id, request);
//
//            // THEN
//            verify(mapper).updateEntity(existingEntity, request);
//
//            ArgumentCaptor<CatOutstandingType> captor = ArgumentCaptor.forClass(CatOutstandingType.class);
//            verify(repository).save(captor.capture());
//            CatOutstandingType updated = captor.getValue();
//
//            assertEquals(fixedTime, updated.getUpdatedAt(), "Phải cập nhật UpdatedAt khi sửa đổi");
//            assertEquals(id, updated.getId());
//            verify(repository, times(1)).save(existingEntity);
//        }
//
//        @Test
//        @DisplayName("TC_04: Cập nhật thất bại - ID không tồn tại hoặc đã xóa")
//        void update_fail_notFound() {
//            // GIVEN
//            Long id = 999L;
//            OutstandingTypeDto request = OutstandingTypeDto.builder().typeCode("A").typeName("B").build();
//
//            when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
//            when(translator.getMessage("catOutstandingType.notFound", id)).thenReturn("Không tìm thấy ID: " + id);
//
//            // WHEN & THEN
//            CustomException ex = assertThrows(CustomException.class, () -> service.update(id, request));
//            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
//            assertTrue(ex.getMessage().contains(id.toString()));
//            verify(repository, never()).save(any());
//        }
//    }
//
////    // =================================================================
////    // NHÓM TEST XÓA (DELETE)
////    // =================================================================
////    @Nested
////    @DisplayName("delete(): Xóa CatOutstandingType")
////    class DeleteTests {
////
////        @Test
////        @DisplayName("TC_05: Xóa thành công - Xác minh isDeleted chuyển sang TRUE")
////        void delete_success() {
////            // GIVEN
////            Long id = 5L;
////            CatOutstandingType entity = new CatOutstandingType();
////            entity.setId(id);
////            entity.setIsDeleted(false);
////
////            when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
////            // RULE 3: Stubbing chính xác instance
////            when(repository.save(entity)).thenReturn(entity);
////
////            // WHEN
////            service.delete(id);
////
////            // THEN
////            ArgumentCaptor<CatOutstandingType> captor = ArgumentCaptor.forClass(CatOutstandingType.class);
////            verify(repository).save(captor.capture());
////            CatOutstandingType deletedEntity = captor.getValue();
////
////            assertAll("Xác minh trạng thái xóa logic",
////                    () -> assertTrue(deletedEntity.getIsDeleted(), "Trường isDeleted phải được set là TRUE"),
////                    () -> assertEquals(fixedTime, deletedEntity.getUpdatedAt(), "UpdatedAt phải được cập nhật khi xóa logic")
////            );
////            verify(repository, times(1)).save(entity);
////        }
////
////        @Test
////        @DisplayName("TC_05.1: Xóa thất bại - ID không tồn tại hoặc đã bị xóa trước đó")
////        void delete_fail_notFound() {
////            // GIVEN
////            Long id = 99L;
////            when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
////
////            // Mock translator cho key not found
////            String errorMsg = "Không tìm thấy bản ghi với ID: " + id;
////            when(translator.getMessage("catOutstandingType.notFound", id)).thenReturn(errorMsg);
////
////            // WHEN & THEN
////            CustomException ex = assertThrows(CustomException.class, () -> service.delete(id));
////
////            assertAll("Xác minh thông tin ngoại lệ khi không tìm thấy ID",
////                    () -> assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError(), "Phải trả về mã lỗi 404"),
////                    () -> assertEquals(errorMsg, ex.getMessage(), "Message trả về không khớp với Translator")
////            );
////
////            // Đảm bảo không có hàm save nào được gọi khi không tìm thấy dữ liệu
////            verify(repository, never()).save(any());
////        }
////    }
//
//    // =================================================================
//    // NHÓM TEST TRUY VẤN (QUERIES)
//    // =================================================================
//    @Nested
//    @DisplayName("Kiểm thử các hàm truy vấn")
//    class QueryTests {
//
//        @Test
//        @DisplayName("TC_06: getById - Thành công")
//        void getById_success() {
//            // GIVEN
//            Long id = 1L;
//            CatOutstandingType entity = new CatOutstandingType();
//            entity.setId(id);
//            when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
//            when(mapper.toDto(entity)).thenReturn(OutstandingTypeDto.builder().id(id).build());
//
//            // WHEN
//            OutstandingTypeDto result = service.getById(id);
//
//            // THEN
//            assertEquals(id, result.getId());
//            verify(repository).findByIdAndIsDeletedFalse(id);
//        }
//
//        @Test
//        @DisplayName("TC_07: getAll - Trả về danh sách khi có dữ liệu")
//        void getAll_hasData() {
//            // GIVEN
//            CatOutstandingType e1 = new CatOutstandingType();
//            when(repository.findAllByIsDeletedFalse()).thenReturn(List.of(e1));
//            when(mapper.toDto(e1)).thenReturn(OutstandingTypeDto.builder().build());
//
//            // WHEN
//            List<OutstandingTypeDto> result = service.getAll();
//
//            // THEN
//            assertFalse(result.isEmpty());
//            assertEquals(1, result.size());
//            verify(repository).findAllByIsDeletedFalse();
//            verify(mapper, times(1)).toDto(any());
//        }
//
//        @Test
//        @DisplayName("TC_08: getAll - Trả về danh sách rỗng")
//        void getAll_noData() {
//            // GIVEN
//            when(repository.findAllByIsDeletedFalse()).thenReturn(Collections.emptyList());
//
//            // WHEN
//            List<OutstandingTypeDto> result = service.getAll();
//
//            // THEN
//            assertTrue(result.isEmpty());
//            verify(mapper, never()).toDto(any());
//        }
//    }
//}
