package vn.com.viettel.services.impl;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.entities.Attachment;
import vn.com.viettel.entities.Recommendation;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.AttachmentMapper;
import vn.com.viettel.minio.dto.ObjectFileDTO;
import vn.com.viettel.repositories.jpa.AttachmentRepository;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.repositories.jpa.RecommendationRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.StorageService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private SysUserRepository userRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private Translator translator;
    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private OutstandingItemRepository outstandingItemRepository;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @Captor
    private ArgumentCaptor<Attachment> attachmentCaptor;
    @Captor
    private ArgumentCaptor<List<Attachment>> attachmentListCaptor;

    private final String BUCKET_NAME = "test-bucket";
    private final String ATTACHMENT_CHANNEL = "attachments";
    private final Long DEFAULT_USER_ID = Constants.DEFAULT_USER_ID;
    private final Long TEST_REF_ID = 100L;
    private final String TEST_REF_TYPE_REC = Constants.RECOMMENDATION_REFERENCE_TYPE;
    private final String TEST_FILE_NAME = "test.pdf";
    private final String TEST_FILE_PATH = "path/to/test.pdf";
    private final String TEST_FILE_URL = "http://minio/test.pdf";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "bucketName", BUCKET_NAME);
    }

    // ================= PRIVATE HELPERS FOR TEST =================
    private void mockTranslator(String key, String message) {
        lenient().when(translator.getMessage(eq(key), any())).thenReturn(message);
        lenient().when(translator.getMessage(eq(key))).thenReturn(message);
    }

    private SysUser createMockUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private void runWithMockedSecurityContext(SysUser user, Runnable runnable) {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(user != null);
            if (user != null) {
                when(authentication.getPrincipal()).thenReturn(user);
            }
            runnable.run();
        }
    }

    // ================= TESTS START HERE =================

    @Nested
    @DisplayName("GROUP 1: uploadAttachment")
    class UploadAttachmentTest {

        @Test
        @DisplayName("Case: File is null -> Throw Exception (400)")
        void upload_FileNull_ThrowsException() {
            // GIVEN
            mockTranslator("attachment.file.required", "File required");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(TEST_REF_ID, TEST_REF_TYPE_REC, null));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
            assertEquals("File required", ex.getMessage());
        }

        @Test
        @DisplayName("Case: File is empty -> Throw Exception (400)")
        void upload_FileEmpty_ThrowsException() {
            // GIVEN
            MultipartFile emptyFile = mock(MultipartFile.class);
            when(emptyFile.isEmpty()).thenReturn(true);
            mockTranslator("attachment.file.required", "File required");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(TEST_REF_ID, TEST_REF_TYPE_REC, emptyFile));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: RefID provided but RefType null -> Throw Exception (400 incomplete)")
        void upload_RefIdOnly_ThrowsException() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            mockTranslator("attachment.reference.incomplete", "Incomplete reference");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(TEST_REF_ID, null, file));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
            assertEquals("Incomplete reference", ex.getMessage());
        }

        @Test
        @DisplayName("Case: RefType provided but RefID null -> Throw Exception (400 incomplete)")
        void upload_RefTypeOnly_ThrowsException() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            mockTranslator("attachment.reference.incomplete", "Incomplete reference");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(null, TEST_REF_TYPE_REC, file));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: Invalid RefType -> Throw Exception (400 invalid)")
        void upload_RefTypeInvalid_ThrowsException() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            String invalidType = "INVALID_TYPE";
            mockTranslator("attachment.referenceType.invalid", "Invalid type");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(TEST_REF_ID, invalidType, file));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
            assertEquals("Invalid type", ex.getMessage());
        }

        @Test
        @DisplayName("Case: RefType RECOMMENDATION not found -> Throw Exception (404)")
        void upload_RefRecommendation_NotFound_Throws() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            when(recommendationRepository.findByIdAndIsDeletedFalse(TEST_REF_ID)).thenReturn(Optional.empty());
            mockTranslator("recommendation.notFound", "Rec not found");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(TEST_REF_ID, TEST_REF_TYPE_REC, file));

            // THEN
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: RefType OUTSTANDING not found -> Throw Exception (404)")
        void upload_RefOutstanding_NotFound_Throws() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            String refType = Constants.OUTSTANDING_REFERENCE_TYPE;
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(TEST_REF_ID)).thenReturn(Optional.empty());
            mockTranslator("outstandingitem.notFound", "Outstanding not found");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(TEST_REF_ID, refType, file));

            // THEN
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: Storage service returns null -> Throw Exception (500)")
        void upload_StorageReturnsNull_Throws() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            when(storageService.uploadFiles(eq(BUCKET_NAME), eq(ATTACHMENT_CHANNEL), any())).thenReturn(null);
            mockTranslator("attachment.upload.failed", "Upload failed");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(null, null, file));

            // THEN
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: Storage service returns empty list -> Throw Exception (500)")
        void upload_StorageReturnsEmpty_Throws() {
            // GIVEN
            MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "content".getBytes());
            when(storageService.uploadFiles(eq(BUCKET_NAME), eq(ATTACHMENT_CHANNEL), any())).thenReturn(Collections.emptyList());
            mockTranslator("attachment.upload.failed", "Upload failed");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.uploadAttachment(null, null, file));

            // THEN
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Happy Path: Upload success with User logged in and valid Reference")
        void upload_Success_WithUser_AndRef() {
            SysUser mockUser = createMockUser(99L, "user99");
            runWithMockedSecurityContext(mockUser,() -> {
                // GIVEN
                MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "application/pdf", "pdf-content".getBytes());

                // Mock validation check
                when(recommendationRepository.findByIdAndIsDeletedFalse(TEST_REF_ID)).thenReturn(Optional.of(new Recommendation()));

                // Mock storage upload
                ObjectFileDTO minioFile = ObjectFileDTO.builder()
                        .filePath(TEST_FILE_PATH).linkUrlPublic(TEST_FILE_URL).build();
                when(storageService.uploadFiles(eq(BUCKET_NAME), eq(ATTACHMENT_CHANNEL), any()))
                        .thenReturn(List.of(minioFile));

                // Mock DB save
                Attachment savedEntity = new Attachment();
                savedEntity.setId(555L);
                when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedEntity);

                // Mock mapper
                AttachmentDto expectedDto = new AttachmentDto();
                expectedDto.setId(555L);
                when(attachmentMapper.mapToDto(savedEntity)).thenReturn(expectedDto);

                // WHEN
                AttachmentDto result = attachmentService.uploadAttachment(TEST_REF_ID, TEST_REF_TYPE_REC, file);

                // THEN
                assertNotNull(result);
                assertEquals(555L, result.getId());

                verify(attachmentRepository).save(attachmentCaptor.capture());
                Attachment captured = attachmentCaptor.getValue();
                assertEquals(TEST_REF_ID, captured.getReferenceId());
                assertEquals(TEST_REF_TYPE_REC, captured.getReferenceType());
                assertEquals(TEST_FILE_NAME, captured.getFileName());
                assertEquals("pdf", captured.getFileExt()); // Check extension calculation
                assertEquals(TEST_FILE_PATH, captured.getFilePath());
                assertEquals(TEST_FILE_URL, captured.getFileUrl());
                assertEquals(mockUser.getId(), captured.getUploadedBy());
                assertNotNull(captured.getUploadedAt());
                assertFalse(captured.getIsDeleted());
            });
        }

        @Test
        @DisplayName("Happy Path: Upload success with No User (Anonymous) and No Reference, File no extension")
        void upload_Success_NoUser_NoRef_FileNoExt() {
            runWithMockedSecurityContext(null, () -> {
                // GIVEN
                String fileNameNoExt = "README";
                MultipartFile file = new MockMultipartFile("file", fileNameNoExt, "text/plain", "bytes".getBytes());

                // Mock storage upload
                ObjectFileDTO minioFile = ObjectFileDTO.builder().filePath("path").linkUrlPublic("url").build();
                when(storageService.uploadFiles(anyString(), anyString(), any())).thenReturn(List.of(minioFile));

                // Mock DB save
                when(attachmentRepository.save(any(Attachment.class))).thenAnswer(i -> i.getArguments()[0]);
                when(attachmentMapper.mapToDto(any())).thenReturn(new AttachmentDto());

                // WHEN
                attachmentService.uploadAttachment(null, null, file);

                // THEN
                verify(attachmentRepository).save(attachmentCaptor.capture());
                Attachment captured = attachmentCaptor.getValue();
                assertNull(captured.getReferenceId());
                assertNull(captured.getReferenceType());
                assertEquals(fileNameNoExt, captured.getFileName());
                assertEquals("", captured.getFileExt()); // Check empty extension correctly
                assertEquals(DEFAULT_USER_ID, captured.getUploadedBy()); // Default user
            });
        }

        @Test
        @DisplayName("CRITICAL: DB Save fails -> Should ROLLBACK MinIO file (Compensating Transaction)")
        void upload_DbError_ShouldRollbackMinio() {
            runWithMockedSecurityContext(null, () -> {
                // GIVEN
                MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "bytes".getBytes());
                ObjectFileDTO minioFile = ObjectFileDTO.builder().filePath(TEST_FILE_PATH).build();

                // Storage upload succeeds
                when(storageService.uploadFiles(anyString(), anyString(), any())).thenReturn(List.of(minioFile));

                // DB save fails
                RuntimeException dbError = new RuntimeException("DB connection failed");
                when(attachmentRepository.save(any(Attachment.class))).thenThrow(dbError);

                // WHEN
                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> attachmentService.uploadAttachment(null, null, file));

                // THEN
                assertEquals(dbError.getMessage(), ex.getMessage());
                // Verify rollback called EXACTLY once with correct parameters
                verify(storageService, times(1)).deleteFile(BUCKET_NAME, TEST_FILE_PATH);
            });
        }

        @Test
        @DisplayName("Edge Case: DB Save fails AND Rollback fails -> Should log and still throw DB exception")
        void upload_DbError_RollbackFail_ShouldLog() {
            runWithMockedSecurityContext(null, () -> {
                // GIVEN
                MultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "bytes".getBytes());
                ObjectFileDTO minioFile = ObjectFileDTO.builder().filePath(TEST_FILE_PATH).build();

                when(storageService.uploadFiles(anyString(), anyString(), any())).thenReturn(List.of(minioFile));
                RuntimeException dbError = new RuntimeException("DB Error");
                when(attachmentRepository.save(any(Attachment.class))).thenThrow(dbError);

                // Rollback also fails
                doThrow(new RuntimeException("MinIO down")).when(storageService).deleteFile(anyString(), anyString());

                // WHEN
                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> attachmentService.uploadAttachment(null, null, file));

                // THEN
                assertEquals(dbError.getMessage(), ex.getMessage());
                verify(storageService, times(1)).deleteFile(BUCKET_NAME, TEST_FILE_PATH);
                // We cannot easily assert logs with Mockito alone, but we ensure the flow didn't crash due to rollback error.
            });
        }
    }

    @Nested
    @DisplayName("GROUP 2: getAttachments")
    class GetAttachmentsTest {

        @Test
        @DisplayName("Case: RefId is null -> Throw Exception (400)")
        void getList_RefIdNull_Throws() {
            mockTranslator("attachment.referenceId.null", "RefId null");
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.getAttachments(null, TEST_REF_TYPE_REC));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: RefType is blank -> Throw Exception (400)")
        void getList_RefTypeBlank_Throws() {
            mockTranslator("attachment.referenceType.required", "RefType required");
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.getAttachments(TEST_REF_ID, "  "));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: Reference not found (Validation fail) -> Throw Exception (404)")
        void getList_RefNotFound_Throws() {
            when(recommendationRepository.findByIdAndIsDeletedFalse(TEST_REF_ID)).thenReturn(Optional.empty());
            mockTranslator("recommendation.notFound", "Not found");

            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.getAttachments(TEST_REF_ID, TEST_REF_TYPE_REC));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Happy Path: Get list success")
        void getList_Success() {
            // GIVEN
            when(recommendationRepository.findByIdAndIsDeletedFalse(TEST_REF_ID)).thenReturn(Optional.of(new Recommendation()));

            List<Attachment> entities = List.of(new Attachment(), new Attachment());
            when(attachmentRepository.findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(TEST_REF_ID, TEST_REF_TYPE_REC))
                    .thenReturn(entities);

            List<AttachmentDto> dtos = List.of(new AttachmentDto(), new AttachmentDto());
            when(attachmentMapper.mapToDtos(entities)).thenReturn(dtos);

            // WHEN
            List<AttachmentDto> result = attachmentService.getAttachments(TEST_REF_ID, TEST_REF_TYPE_REC);

            // THEN
            assertEquals(2, result.size());
            verify(attachmentMapper).mapToDtos(entities);
        }
    }

    @Nested
    @DisplayName("GROUP 3: getDetail & download")
    class GetDetailAndDownloadTest {

        private final Long ATTACH_ID = 777L;

        @Test
        @DisplayName("Case: ID null -> Throw Exception (400)")
        void getDetail_IdNull_Throws() {
            mockTranslator("attachment.id.null", "ID null");
            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.getAttachmentDetail(null));
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: ID not found in DB -> Throw Exception (404)")
        void getDetail_NotFound_Throws() {
            when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.empty());
            mockTranslator("attachment.notFound", "Not found");

            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.getAttachmentDetail(ATTACH_ID));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case: ID found but isDeleted=true -> Throw Exception (404)")
        void getDetail_Deleted_Throws() {
            Attachment deletedAtt = new Attachment();
            deletedAtt.setIsDeleted(true);
            when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.of(deletedAtt));
            mockTranslator("attachment.notFound", "Not found");

            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.getAttachmentDetail(ATTACH_ID));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Happy Path: Get detail success")
        void getDetail_Success() {
            Attachment att = new Attachment();
            att.setIsDeleted(false);
            when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.of(att));
            AttachmentDto dto = new AttachmentDto();
            when(attachmentMapper.mapToDto(att)).thenReturn(dto);

            AttachmentDto result = attachmentService.getAttachmentDetail(ATTACH_ID);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Download Case: Storage throws error -> Throw Exception (500)")
        void download_StorageError_Throws() {
            Attachment att = new Attachment();
            att.setFilePath(TEST_FILE_PATH);
            att.setIsDeleted(false);
            when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.of(att));

            when(storageService.getFile(BUCKET_NAME, TEST_FILE_PATH)).thenThrow(new RuntimeException("MinIO err"));
            mockTranslator("attachment.download.failed", "Download failed");

            CustomException ex = assertThrows(CustomException.class,
                    () -> attachmentService.downloadAttachment(ATTACH_ID));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Happy Path: Download success")
        void download_Success() {
            Attachment att = new Attachment();
            att.setFilePath(TEST_FILE_PATH);
            att.setIsDeleted(false);
            when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.of(att));
            byte[] expectedBytes = "file content".getBytes();
            when(storageService.getFile(BUCKET_NAME, TEST_FILE_PATH)).thenReturn(expectedBytes);

            byte[] result = attachmentService.downloadAttachment(ATTACH_ID);
            assertArrayEquals(expectedBytes, result);
        }
    }

    @Nested
    @DisplayName("GROUP 4: Delete (Soft Delete)")
    class DeleteAttachmentTest {
        private final Long ATTACH_ID = 888L;

        @Test
        @DisplayName("Happy Path: Soft delete success with User")
        void delete_Success_WithUser() {
            SysUser mockUser = createMockUser(123L, "user123");
            runWithMockedSecurityContext(mockUser, () -> {
                Attachment att = new Attachment();
                att.setId(ATTACH_ID);
                att.setIsDeleted(false);
                when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.of(att));

                // WHEN
                attachmentService.deleteAttachment(ATTACH_ID);

                // THEN
                verify(attachmentRepository).save(attachmentCaptor.capture());
                Attachment captured = attachmentCaptor.getValue();
                assertEquals(ATTACH_ID, captured.getId());
                assertTrue(captured.getIsDeleted());
                assertEquals(mockUser.getId(), captured.getUpdatedBy());
                assertNotNull(captured.getUpdatedAt());
            });
        }

        @Test
        @DisplayName("Happy Path: Soft delete success by Anonymous (Default User)")
        void delete_Success_Anonymous() {
            runWithMockedSecurityContext(null, () -> {
                Attachment att = new Attachment();
                att.setId(ATTACH_ID);
                att.setIsDeleted(false);
                when(attachmentRepository.findById(ATTACH_ID)).thenReturn(Optional.of(att));

                // WHEN
                attachmentService.deleteAttachment(ATTACH_ID);

                // THEN
                verify(attachmentRepository).save(attachmentCaptor.capture());
                Attachment captured = attachmentCaptor.getValue();
                assertTrue(captured.getIsDeleted());
                assertEquals(DEFAULT_USER_ID, captured.getUpdatedBy());
            });
        }
    }

    @Nested
    @DisplayName("GROUP 5: Batch Operations")
    class BatchOperationsTest {

        @Test
        @DisplayName("handleAttachment: Files null -> return empty, no save")
        void handle_FilesNull_ReturnEmpty() {
            List<Attachment> result = attachmentService.handleAttachment(null, TEST_REF_ID, TEST_REF_TYPE_REC, ATTACHMENT_CHANNEL);
            assertTrue(result.isEmpty());
            verify(attachmentRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("handleAttachment: Success -> saveAll called with correct data")
        void handle_Success() {
             SysUser mockUser = createMockUser(111L, "user111");
             runWithMockedSecurityContext(mockUser, () -> {
                 // GIVEN
                 MultipartFile[] files = {new MockMultipartFile("f1", "f1.txt", "text", "c1".getBytes())};
                 ObjectFileDTO objDto = ObjectFileDTO.builder().fileName("f1.txt").filePath("p1").fileSize(10L).linkUrlPublic("u1").build();
                 when(storageService.uploadFiles(eq(BUCKET_NAME), eq(ATTACHMENT_CHANNEL), any())).thenReturn(List.of(objDto));

                 // WHEN
                 attachmentService.handleAttachment(files, TEST_REF_ID, TEST_REF_TYPE_REC, ATTACHMENT_CHANNEL);

                 // THEN
                 verify(attachmentRepository).saveAll(attachmentListCaptor.capture());
                 List<Attachment> capturedList = attachmentListCaptor.getValue();
                 assertEquals(1, capturedList.size());
                 Attachment att = capturedList.getFirst();
                 assertEquals("f1.txt", att.getFileName());
                 assertEquals(TEST_REF_ID, att.getReferenceId());
                 assertEquals(mockUser.getId(), att.getUploadedBy());
                 assertFalse(att.getIsDeleted());
             });
        }

        @Test
        @DisplayName("deleteAttachments (Batch by Ref): Success -> soft delete all")
        void deleteBatch_ByRef_Success() {
            List<Long> refIds = List.of(1L, 2L);
            String refType = "TYPE";
            Long userId = 99L;

            Attachment a1 = new Attachment(); a1.setId(10L); a1.setIsDeleted(false);
            Attachment a2 = new Attachment(); a2.setId(20L); a2.setIsDeleted(false);
            List<Attachment> list = Arrays.asList(a1, a2);

            when(attachmentRepository.findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(refIds, refType))
                    .thenReturn(list);

            // WHEN
            attachmentService.deleteAttachments(refIds, refType, userId);

            // THEN
            verify(attachmentRepository).saveAll(attachmentListCaptor.capture());
            List<Attachment> capturedList = attachmentListCaptor.getValue();
            assertEquals(2, capturedList.size());
            assertTrue(capturedList.get(0).getIsDeleted());
            assertEquals(userId, capturedList.get(0).getUpdatedBy());
            assertTrue(capturedList.get(1).getIsDeleted());
        }

        @Test
        @DisplayName("deleteAttachmentsById (Batch by ID): Success -> soft delete all")
        void deleteBatch_ById_Success() {
            List<Long> ids = List.of(10L, 20L);
            Long userId = 88L;

            Attachment a1 = new Attachment(); a1.setId(10L); a1.setIsDeleted(false);
            Attachment a2 = new Attachment(); a2.setId(20L); a2.setIsDeleted(false);
            List<Attachment> list = Arrays.asList(a1, a2);

            when(attachmentRepository.findAllByIdInAndIsDeletedFalse(ids)).thenReturn(list);

            // WHEN
            attachmentService.deleteAttachmentsById(ids, userId);

            // THEN
            verify(attachmentRepository).saveAll(attachmentListCaptor.capture());
            List<Attachment> capturedList = attachmentListCaptor.getValue();
            assertEquals(2, capturedList.size());
            assertTrue(capturedList.stream().allMatch(Attachment::getIsDeleted));
            assertEquals(userId, capturedList.getFirst().getUpdatedBy());
        }
    }
}