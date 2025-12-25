package vn.com.viettel.services.impl;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.dto.OutstandingProcessActionEnum;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.dto.OutstandingStatusEnum;
import vn.com.viettel.entities.OutstandingItem;
import vn.com.viettel.entities.OutstandingProcessLog;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.OutstandingProcessLogMapper;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.repositories.jpa.OutstandingProcessLogRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutstandingProcessLogServiceImplTest {

    @InjectMocks
    private OutstandingProcessLogServiceImpl service;

    @Mock
    private OutstandingProcessLogRepository repository;
    @Mock
    private OutstandingItemRepository outstandingItemRepository;
    @Mock
    private OutstandingProcessLogMapper mapper;
    @Mock
    private Translator translator;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private SysUserRepository userRepository;

    private static final Long OUTSTANDING_ID = 100L;
    private static final Long PROCESS_ID = 200L;
    private static final Long USER_ID = 999L;
    private static final String USERNAME = "test_user";
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 12, 25, 10, 0, 0);

    private OutstandingProcessLogDto requestDto;
    private MultipartFile[] attachments;

    @BeforeEach
    void setUp() {
        // FIX: Sử dụng Builder thay vì Constructor
        requestDto = OutstandingProcessLogDto.builder()
                .processContent("Test Content")
                .actionType(OutstandingProcessActionEnum.SAVE_RESULT)
                .build();
        attachments = new MultipartFile[]{};
    }

    @Nested
    @DisplayName("Method: create")
    class CreateTests {

        @Test
        @DisplayName("Case 1.1: create_OutstandingNotFound_ThrowException")
        void create_OutstandingNotFound_ThrowException() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(false);
            when(translator.getMessage("outstanding.notfound", OUTSTANDING_ID)).thenReturn("Item not found");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> service.create(OUTSTANDING_ID, requestDto, attachments));

            // THEN
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
            assertEquals("Item not found", ex.getMessage());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Case 1.2: create_RequestNull_ThrowException")
        void create_RequestNull_ThrowException() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            when(translator.getMessage("invalid.request")).thenReturn("Invalid Request");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> service.create(OUTSTANDING_ID, null, attachments));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
            assertEquals("Invalid Request", ex.getMessage());
        }

        @Test
        @DisplayName("Case 1.4: create_ContentEmpty_ThrowException")
        void create_ContentEmpty_ThrowException() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);

            // FIX: Sử dụng Setter (Class có @Setter)
            requestDto.setProcessContent("");

            when(translator.getMessage("invalid.request")).thenReturn("Invalid Request");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> service.create(OUTSTANDING_ID, requestDto, attachments));

            // THEN
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case 1.5: create_Success_WithPrincipalUser")
        void create_Success_WithPrincipalUser() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);

            OutstandingProcessLog entityToSave = new OutstandingProcessLog();
            entityToSave.setId(PROCESS_ID);
            when(mapper.toEntity(requestDto)).thenReturn(entityToSave);
            when(mapper.toDto(any())).thenReturn(requestDto);

            // Mock Security Context with SysUser
            SysUser mockUser = new SysUser();
            mockUser.setId(USER_ID);
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(mockUser);

            try (MockedStatic<SecurityContextHolder> securityStatic = mockStatic(SecurityContextHolder.class);
                 MockedStatic<LocalDateTime> timeStatic = mockStatic(LocalDateTime.class)) {

                securityStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                timeStatic.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                // WHEN
                OutstandingProcessLogDto result = service.create(OUTSTANDING_ID, requestDto, attachments);

                // THEN
                assertNotNull(result);

                ArgumentCaptor<OutstandingProcessLog> captor = ArgumentCaptor.forClass(OutstandingProcessLog.class);
                verify(repository).save(captor.capture());
                OutstandingProcessLog savedEntity = captor.getValue();

                assertEquals(OUTSTANDING_ID, savedEntity.getOutstandingId());
                assertEquals(Boolean.FALSE, savedEntity.getIsDeleted());
                assertEquals(FIXED_TIME, savedEntity.getCreatedAt());
                assertEquals(USER_ID, savedEntity.getCreatedBy());

                String expectedChannel = Constants.OUTSTANDING_REFERENCE_TYPE + "/" + OUTSTANDING_ID + "/" + Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE;
                verify(attachmentService).handleAttachment(attachments, PROCESS_ID, Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE, expectedChannel);
            }
        }

        @Test
        @DisplayName("Case 1.6: create_Success_AnonymousUser")
        void create_Success_AnonymousUser() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            OutstandingProcessLog entityToSave = new OutstandingProcessLog();
            entityToSave.setId(PROCESS_ID);
            when(mapper.toEntity(requestDto)).thenReturn(entityToSave);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);

            try (MockedStatic<SecurityContextHolder> securityStatic = mockStatic(SecurityContextHolder.class);
                 MockedStatic<LocalDateTime> timeStatic = mockStatic(LocalDateTime.class)) {

                securityStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                timeStatic.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                // WHEN
                service.create(OUTSTANDING_ID, requestDto, attachments);

                // THEN
                ArgumentCaptor<OutstandingProcessLog> captor = ArgumentCaptor.forClass(OutstandingProcessLog.class);
                verify(repository).save(captor.capture());
                assertEquals(Constants.DEFAULT_USER_ID, captor.getValue().getCreatedBy());
            }
        }
    }

    @Nested
    @DisplayName("Method: update")
    class UpdateTests {

        @Test
        @DisplayName("Case 2.3: update_OutstandingIdMismatch_ThrowException")
        void update_OutstandingIdMismatch_ThrowException() {
            // GIVEN
            OutstandingItem outstandingItem = new OutstandingItem();
            outstandingItem.setId(OUTSTANDING_ID);
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(Optional.of(outstandingItem));

            OutstandingProcessLog entity = new OutstandingProcessLog();
            entity.setId(PROCESS_ID);
            entity.setOutstandingId(OUTSTANDING_ID + 999); // Mismatch ID
            when(repository.findByIdAndIsDeletedFalse(PROCESS_ID)).thenReturn(Optional.of(entity));

            when(translator.getMessage("outstanding.process.notfound", PROCESS_ID)).thenReturn("Process not found");

            // WHEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> service.update(OUTSTANDING_ID, PROCESS_ID, requestDto, attachments));

            // THEN
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
            assertEquals("Process not found", ex.getMessage());
        }

        @Test
        @DisplayName("Case 2.4: update_Success_ActionSaveResult")
        void update_Success_ActionSaveResult() {
            // GIVEN
            requestDto.setActionType(OutstandingProcessActionEnum.SAVE_RESULT);

            // FIX: Sử dụng AttachmentDto Builder
            AttachmentDto attachmentDto = AttachmentDto.builder()
                    .id(1L)
                    .fileName("delete_me.txt")
                    .build();
            requestDto.setDeletedAttachments(List.of(attachmentDto));

            OutstandingItem outstandingItem = new OutstandingItem();
            outstandingItem.setId(OUTSTANDING_ID);
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(Optional.of(outstandingItem));

            OutstandingProcessLog entity = new OutstandingProcessLog();
            entity.setId(PROCESS_ID);
            entity.setOutstandingId(OUTSTANDING_ID);
            when(repository.findByIdAndIsDeletedFalse(PROCESS_ID)).thenReturn(Optional.of(entity));

            // Mock Authentication with String Username
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(USERNAME);
            when(auth.getName()).thenReturn(USERNAME);

            SysUser dbUser = new SysUser();
            dbUser.setId(USER_ID);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(dbUser));

            try (MockedStatic<SecurityContextHolder> securityStatic = mockStatic(SecurityContextHolder.class);
                 MockedStatic<LocalDateTime> timeStatic = mockStatic(LocalDateTime.class)) {

                securityStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                timeStatic.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                // WHEN
                service.update(OUTSTANDING_ID, PROCESS_ID, requestDto, attachments);

                // THEN
                ArgumentCaptor<OutstandingProcessLog> logCaptor = ArgumentCaptor.forClass(OutstandingProcessLog.class);
                verify(repository).save(logCaptor.capture());
                assertEquals(FIXED_TIME, logCaptor.getValue().getUpdatedAt());
                assertEquals(USER_ID, logCaptor.getValue().getUpdatedBy());

                verify(mapper).updateEntity(entity, requestDto);

                // Verify Attachment Logic
                verify(attachmentService).handleAttachment(any(), eq(PROCESS_ID), any(), any());
                verify(attachmentService).deleteAttachments(eq(List.of(PROCESS_ID)), eq(Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE), eq(USER_ID));

                ArgumentCaptor<OutstandingItem> itemCaptor = ArgumentCaptor.forClass(OutstandingItem.class);
                verify(outstandingItemRepository).save(itemCaptor.capture());
                OutstandingItem savedItem = itemCaptor.getValue();
                assertEquals(OutstandingStatusEnum.IN_PROGRESS.name(), savedItem.getStatus());
                assertEquals(FIXED_TIME, savedItem.getLastUpdateAt());
                assertEquals(USER_ID, savedItem.getLastUpdateBy());
            }
        }

        @Test
        @DisplayName("Case 2.5: update_Success_ActionSendAcceptance")
        void update_Success_ActionSendAcceptance() {
            // GIVEN
            requestDto.setActionType(OutstandingProcessActionEnum.SEND_FOR_ACCEPTANCE);

            OutstandingItem outstandingItem = new OutstandingItem();
            outstandingItem.setId(OUTSTANDING_ID);
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(Optional.of(outstandingItem));

            OutstandingProcessLog entity = new OutstandingProcessLog();
            entity.setId(PROCESS_ID);
            entity.setOutstandingId(OUTSTANDING_ID);
            when(repository.findByIdAndIsDeletedFalse(PROCESS_ID)).thenReturn(Optional.of(entity));

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);

            try (MockedStatic<SecurityContextHolder> securityStatic = mockStatic(SecurityContextHolder.class);
                 MockedStatic<LocalDateTime> timeStatic = mockStatic(LocalDateTime.class)) {

                securityStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                timeStatic.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                // WHEN
                service.update(OUTSTANDING_ID, PROCESS_ID, requestDto, attachments);

                // THEN
                ArgumentCaptor<OutstandingItem> itemCaptor = ArgumentCaptor.forClass(OutstandingItem.class);
                verify(outstandingItemRepository).save(itemCaptor.capture());
                assertEquals(OutstandingStatusEnum.DONE.name(), itemCaptor.getValue().getStatus());
                assertEquals(Constants.DEFAULT_USER_ID, itemCaptor.getValue().getLastUpdateBy());
            }
        }
    }

    @Nested
    @DisplayName("Method: delete")
    class DeleteTests {

        @Test
        @DisplayName("Case 3.1: delete_Success")
        void delete_Success() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);

            OutstandingProcessLog entity = new OutstandingProcessLog();
            entity.setId(PROCESS_ID);
            entity.setOutstandingId(OUTSTANDING_ID);
            entity.setIsDeleted(false);
            when(repository.findByIdAndIsDeletedFalse(PROCESS_ID)).thenReturn(Optional.of(entity));

            SysUser mockUser = new SysUser();
            mockUser.setId(USER_ID);
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(mockUser);

            try (MockedStatic<SecurityContextHolder> securityStatic = mockStatic(SecurityContextHolder.class);
                 MockedStatic<LocalDateTime> timeStatic = mockStatic(LocalDateTime.class)) {

                securityStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                timeStatic.when(LocalDateTime::now).thenReturn(FIXED_TIME);

                // WHEN
                service.delete(OUTSTANDING_ID, PROCESS_ID);

                // THEN
                ArgumentCaptor<OutstandingProcessLog> captor = ArgumentCaptor.forClass(OutstandingProcessLog.class);
                verify(repository).save(captor.capture());

                OutstandingProcessLog deletedEntity = captor.getValue();
                assertEquals(Boolean.TRUE, deletedEntity.getIsDeleted());
                assertEquals(FIXED_TIME, deletedEntity.getUpdatedAt());
                assertEquals(USER_ID, deletedEntity.getUpdatedBy());

                verify(attachmentService).deleteAttachments(List.of(PROCESS_ID), Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE, USER_ID);
            }
        }
    }

    @Nested
    @DisplayName("Method: Read (getById, getAll)")
    class ReadTests {

        @Test
        @DisplayName("Case 4.1: getById_Success")
        void getById_Success() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            OutstandingProcessLog entity = new OutstandingProcessLog();
            entity.setId(PROCESS_ID);
            entity.setOutstandingId(OUTSTANDING_ID);
            when(repository.findByIdAndIsDeletedFalse(PROCESS_ID)).thenReturn(Optional.of(entity));

            // FIX: Sử dụng Builder
            OutstandingProcessLogDto expectedDto = OutstandingProcessLogDto.builder()
                    .processId(PROCESS_ID)
                    .build();
            when(mapper.toDtoList(List.of(entity))).thenReturn(List.of(expectedDto));

            // WHEN
            OutstandingProcessLogDto result = service.getById(OUTSTANDING_ID, PROCESS_ID);

            // THEN
            assertNotNull(result);
            assertEquals(expectedDto, result);
        }

        @Test
        @DisplayName("Case 4.2: getAll_Success")
        void getAll_Success() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            when(repository.findAllByOutstandingIdAndIsDeletedFalseOrderByUpdatedAtDesc(OUTSTANDING_ID))
                    .thenReturn(Collections.emptyList());
            when(mapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // WHEN
            List<OutstandingProcessLogDto> result = service.getAll(OUTSTANDING_ID);

            // THEN
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}