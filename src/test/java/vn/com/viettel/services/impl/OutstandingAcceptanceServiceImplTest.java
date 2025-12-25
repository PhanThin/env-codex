package vn.com.viettel.services.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.OutstandingAcceptanceMapper;
import vn.com.viettel.repositories.jpa.OutstandingAcceptanceRepository;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutstandingAcceptanceService Tests - Updated Enum Logic")
class OutstandingAcceptanceServiceImplTest {

    @InjectMocks
    private OutstandingAcceptanceServiceImpl service;

    @Mock
    private OutstandingAcceptanceRepository repository;
    @Mock
    private OutstandingItemRepository outstandingItemRepository;
    @Mock
    private OutstandingAcceptanceMapper mapper;
    @Mock
    private Translator translator;
    @Mock
    private SysUserRepository userRepository;
    @Mock
    private AttachmentService attachmentService;

    @Captor
    private ArgumentCaptor<OutstandingAcceptance> acceptanceCaptor;
    @Captor
    private ArgumentCaptor<OutstandingItem> itemCaptor;

    // Constants
    private static final Long OUTSTANDING_ID = 100L;
    private static final Long ACCEPTANCE_ID = 200L;
    private static final Long USER_ID = 99L;
    private static final String USERNAME = "test_user";

    // ==================================================================================
    // GROUP 1: CREATE TESTS
    // ==================================================================================
    @Nested
    @DisplayName("Method: create(...)")
    class CreateTests {
        private OutstandingItem defaultItem;
        private final MultipartFile[] emptyFiles = new MultipartFile[]{};

        @BeforeEach
        void setUp() {
            defaultItem = new OutstandingItem();
            defaultItem.setId(OUTSTANDING_ID);
            defaultItem.setStatus(OutstandingStatusEnum.IN_PROGRESS.name());
        }

        @Test
        @DisplayName("Case 01: Outstanding Item Not Found - Throw 404")
        void create_Case01_OutstandingNotFound() {
            // GIVEN
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                    .thenReturn(Optional.empty());
            when(translator.getMessage(anyString(), any())).thenReturn("Not Found");

            OutstandingAcceptanceDto req = OutstandingAcceptanceDto.builder().build();

            // WHEN & THEN
            CustomException ex = assertThrows(CustomException.class,
                    () -> service.create(OUTSTANDING_ID, req, emptyFiles));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Case 02: Request is Null - Throw 400")
        void create_Case02_RequestNull() {
            // GIVEN
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                    .thenReturn(Optional.of(defaultItem));
            when(translator.getMessage(anyString())).thenReturn("Invalid Request");

            // WHEN & THEN
            assertThrows(CustomException.class,
                () -> service.create(OUTSTANDING_ID, null, emptyFiles));
        }

        @Test
        @DisplayName("Case 03: Request Result is Null - Throw 400")
        void create_Case03_ResultNull() {
            // GIVEN
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                    .thenReturn(Optional.of(defaultItem));
            when(translator.getMessage(anyString())).thenReturn("Invalid Request");

            OutstandingAcceptanceDto reqResultNull = OutstandingAcceptanceDto.builder()
                    .acceptanceNote("Note").build(); // Result is null

            // WHEN & THEN
            assertThrows(CustomException.class,
                () -> service.create(OUTSTANDING_ID, reqResultNull, emptyFiles));
        }

        @Test
        @DisplayName("Case 04: Acceptance Note is Empty - Throw 400")
        void create_Case04_NoteEmpty() {
            // GIVEN
            when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                    .thenReturn(Optional.of(defaultItem));
            when(translator.getMessage(anyString())).thenReturn("Invalid Request");

            OutstandingAcceptanceDto reqNoteEmpty = OutstandingAcceptanceDto.builder()
                    .result(OutstandingAcceptanceResultEnum.ACCEPTED)
                    .acceptanceNote("") // Empty string
                    .build();

            // WHEN & THEN
            assertThrows(CustomException.class,
                () -> service.create(OUTSTANDING_ID, reqNoteEmpty, emptyFiles));
        }

        @Test
        @DisplayName("Case 05: Verify Config ALLOWED_RESULTS matches Enum (Type Safe Check)")
        void create_Case05_VerifyConfiguration() {
            // GIVEN: Access the private static final field
            @SuppressWarnings("unchecked")
            Set<OutstandingAcceptanceResultEnum> allowedResults =
                (Set<OutstandingAcceptanceResultEnum>) ReflectionTestUtils.getField(service, "ALLOWED_RESULTS");

            // THEN: Ensure it strictly contains the Enums
            assertNotNull(allowedResults, "ALLOWED_RESULTS Set must not be null");
            assertTrue(allowedResults.contains(OutstandingAcceptanceResultEnum.ACCEPTED), "Set must contain ACCEPTED");
            assertTrue(allowedResults.contains(OutstandingAcceptanceResultEnum.REJECTED), "Set must contain REJECTED");

            // Ensure no accidental deletion of supported values
            assertEquals(2, allowedResults.size(), "Set should contain exactly 2 allowed statuses");
        }

        @Test
        @DisplayName("Case 06: Success - Result ACCEPTED -> Item Status CLOSED")
        void create_Case06_Success_Accepted() {
            try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
                setupSecurityContext(mockedSecurity, USER_ID);

                // GIVEN
                when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                        .thenReturn(Optional.of(defaultItem));

                OutstandingAcceptanceDto request = OutstandingAcceptanceDto.builder()
                        .result(OutstandingAcceptanceResultEnum.ACCEPTED)
                        .acceptanceNote("Accepted Note")
                        .build();

                OutstandingAcceptance entity = new OutstandingAcceptance();
                entity.setId(ACCEPTANCE_ID);

                when(mapper.toEntity(request)).thenReturn(entity);
                when(mapper.toDtoList(anyList())).thenReturn(List.of(request));

                // WHEN
                OutstandingAcceptanceDto result = service.create(OUTSTANDING_ID, request, emptyFiles);

                // THEN
                verify(repository).save(acceptanceCaptor.capture());
                // Verify Entity stores the Enum name (String)
                assertEquals("ACCEPTED", acceptanceCaptor.getValue().getResult());
                assertEquals(USER_ID, acceptanceCaptor.getValue().getAcceptedBy());

                verify(outstandingItemRepository).save(itemCaptor.capture());
                assertEquals(OutstandingStatusEnum.CLOSED.name(), itemCaptor.getValue().getStatus());

                verify(attachmentService).handleAttachment(eq(emptyFiles), eq(ACCEPTANCE_ID), anyString(), anyString());
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Case 07: Success - Result REJECTED -> Item Status NEW (Authenticated User)")
        void create_Case07_Success_Rejected() {
            try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
                setupSecurityContext(mockedSecurity, USER_ID);

                // GIVEN
                when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                        .thenReturn(Optional.of(defaultItem));

                OutstandingAcceptanceDto request = OutstandingAcceptanceDto.builder()
                        .result(OutstandingAcceptanceResultEnum.REJECTED)
                        .acceptanceNote("Rejected Note")
                        .build();

                OutstandingAcceptance entity = new OutstandingAcceptance();
                entity.setId(ACCEPTANCE_ID);

                when(mapper.toEntity(request)).thenReturn(entity);
                when(mapper.toDtoList(anyList())).thenReturn(List.of(request));

                // WHEN
                service.create(OUTSTANDING_ID, request, null);

                // THEN
                verify(repository).save(acceptanceCaptor.capture());
                assertEquals("REJECTED", acceptanceCaptor.getValue().getResult());
                assertEquals(USER_ID, acceptanceCaptor.getValue().getAcceptedBy());

                verify(outstandingItemRepository).save(itemCaptor.capture());
                assertEquals(OutstandingStatusEnum.NEW.name(), itemCaptor.getValue().getStatus());
            }
        }

        @Test
        @DisplayName("Case 08: Anonymous User -> Uses Default ID")
        void create_Case08_AnonymousUser() {
            try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
                // GIVEN - Anonymous User (Not Authenticated)
                Authentication auth = mock(Authentication.class);
                SecurityContext context = mock(SecurityContext.class);
                when(context.getAuthentication()).thenReturn(auth);
                when(auth.isAuthenticated()).thenReturn(false);
                mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);

                when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID))
                        .thenReturn(Optional.of(defaultItem));

                OutstandingAcceptanceDto request = OutstandingAcceptanceDto.builder()
                        .result(OutstandingAcceptanceResultEnum.ACCEPTED)
                        .acceptanceNote("Note")
                        .build();

                OutstandingAcceptance entity = new OutstandingAcceptance();
                when(mapper.toEntity(request)).thenReturn(entity);
                when(mapper.toDtoList(anyList())).thenReturn(List.of(request));

                // WHEN
                service.create(OUTSTANDING_ID, request, null);

                // THEN
                verify(repository).save(acceptanceCaptor.capture());
                assertEquals(Constants.DEFAULT_USER_ID, acceptanceCaptor.getValue().getAcceptedBy());

                verify(outstandingItemRepository).save(itemCaptor.capture());
                assertEquals(Constants.DEFAULT_USER_ID, itemCaptor.getValue().getLastUpdateBy());
            }
        }
    }

    // ==================================================================================
    // GROUP 2: UPDATE TESTS
    // ==================================================================================
    @Nested
    @DisplayName("Method: update(...)")
    class UpdateTests {
        private OutstandingAcceptance defaultEntity;

        @BeforeEach
        void setUp() {
            defaultEntity = new OutstandingAcceptance();
            defaultEntity.setId(ACCEPTANCE_ID);
            defaultEntity.setOutstandingId(OUTSTANDING_ID);
            defaultEntity.setIsDeleted(false);
        }

        @Test
        @DisplayName("Case 09: Outstanding Not Exists - Throw 404")
        void update_Case09_OutstandingNotFound() {
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(false);
            when(translator.getMessage(anyString(), any())).thenReturn("Msg");

            OutstandingAcceptanceDto req = OutstandingAcceptanceDto.builder().build();

            CustomException ex = assertThrows(CustomException.class,
                () -> service.update(OUTSTANDING_ID, ACCEPTANCE_ID, req, null));
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case 10: Acceptance Not Found - Throw 404")
        void update_Case10_AcceptanceNotFound() {
            // GIVEN
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(ACCEPTANCE_ID)).thenReturn(Optional.empty());

            // Mock Translator cho message lỗi Not Found
            when(translator.getMessage(anyString(), any())).thenReturn("Msg");

            OutstandingAcceptanceDto req = OutstandingAcceptanceDto.builder()
                    .result(OutstandingAcceptanceResultEnum.ACCEPTED)
                    .acceptanceNote("Any Note")
                    .build();

            // WHEN & THEN
            CustomException ex = assertThrows(CustomException.class,
                () -> service.update(OUTSTANDING_ID, ACCEPTANCE_ID, req, null));

            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case 11: Id Mismatch - Throw 404")
        void update_Case11_IdMismatch() {
            // GIVEN
            defaultEntity.setOutstandingId(9999L);

            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(ACCEPTANCE_ID)).thenReturn(Optional.of(defaultEntity));

            // Mock cho thông báo lỗi ID Mismatch (Hàm này dùng 2 tham số: code + id)
            when(translator.getMessage(anyString(), any())).thenReturn("Not Found Msg");

            // 3. FIX QUAN TRỌNG: Tạo Request HỢP LỆ để vượt qua validateRequest()
            OutstandingAcceptanceDto req = OutstandingAcceptanceDto.builder()
                    .result(OutstandingAcceptanceResultEnum.ACCEPTED) // Bắt buộc có
                    .acceptanceNote("Any note") // Bắt buộc có
                    .build();

            // WHEN & THEN
            CustomException ex = assertThrows(CustomException.class,
                () -> service.update(OUTSTANDING_ID, ACCEPTANCE_ID, req, null));

            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        }

        @Test
        @DisplayName("Case 12: Success - With Attachments Delete")
        void update_Case12_Success() {
            try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
                setupSecurityContext(mockedSecurity, USER_ID);

                when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
                when(repository.findByIdAndIsDeletedFalse(ACCEPTANCE_ID)).thenReturn(Optional.of(defaultEntity));

                AttachmentDto delAtt = AttachmentDto.builder().id(55L).build();
                OutstandingAcceptanceDto request = OutstandingAcceptanceDto.builder()
                        .result(OutstandingAcceptanceResultEnum.REJECTED)
                        .acceptanceNote("Updated Note")
                        .deletedAttachments(List.of(delAtt))
                        .build();

                when(mapper.toDtoList(anyList())).thenReturn(List.of(request));

                // WHEN
                service.update(OUTSTANDING_ID, ACCEPTANCE_ID, request, null);

                // THEN
                verify(mapper).updateEntity(eq(defaultEntity), eq(request));
                verify(repository).save(acceptanceCaptor.capture());
                assertEquals("REJECTED", acceptanceCaptor.getValue().getResult());
                assertEquals(USER_ID, acceptanceCaptor.getValue().getUpdatedBy());

                verify(attachmentService).handleAttachment(any(), eq(ACCEPTANCE_ID), anyString(), anyString());
                verify(attachmentService).deleteAttachmentsById(eq(List.of(55L)), eq(USER_ID));
            }
        }
    }

    // ==================================================================================
    // GROUP 3: READ & DELETE TESTS
    // ==================================================================================
    @Nested
    @DisplayName("Read & Delete Operations")
    class ReadDeleteTests {

        @Test
        @DisplayName("Case 13: Delete Success (Soft Delete)")
        void delete_Case13_Success() {
            try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
                setupSecurityContext(mockedSecurity, USER_ID);

                OutstandingAcceptance entity = new OutstandingAcceptance();
                entity.setId(ACCEPTANCE_ID);
                entity.setOutstandingId(OUTSTANDING_ID);
                entity.setIsDeleted(false);

                when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
                when(repository.findByIdAndIsDeletedFalse(ACCEPTANCE_ID)).thenReturn(Optional.of(entity));

                // WHEN
                service.delete(OUTSTANDING_ID, ACCEPTANCE_ID);

                // THEN
                verify(repository).save(acceptanceCaptor.capture());
                assertTrue(acceptanceCaptor.getValue().getIsDeleted());
                assertEquals(USER_ID, acceptanceCaptor.getValue().getUpdatedBy());
                assertNotNull(acceptanceCaptor.getValue().getUpdatedAt());
            }
        }

        @Test
        @DisplayName("Case 14: Get Detail Success")
        void get_Case14_Success() {
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            when(repository.findByIdAndIsDeletedFalse(ACCEPTANCE_ID)).thenReturn(Optional.of(new OutstandingAcceptance()));
            // FIX: Use Builder for DTO
            when(mapper.toDtoList(anyList())).thenReturn(List.of(OutstandingAcceptanceDto.builder().build()));

            OutstandingAcceptanceDto result = service.get(OUTSTANDING_ID, ACCEPTANCE_ID);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Case 15: Get All Success")
        void getAll_Case15_Success() {
            when(outstandingItemRepository.existsByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(true);
            when(repository.findAllByOutstandingIdAndIsDeletedFalse(OUTSTANDING_ID))
                    .thenReturn(Collections.emptyList());
            when(mapper.toDtoList(anyList())).thenReturn(Collections.emptyList());

            List<OutstandingAcceptanceDto> result = service.getAll(OUTSTANDING_ID);
            assertNotNull(result);
        }
    }

    // ==================================================================================
    // GROUP 4: INTERNAL LOGIC TESTS
    // ==================================================================================
    @Nested
    @DisplayName("Internal Logic / Edge Cases")
    class InternalTests {

        @Test
        @DisplayName("Case 16: Get Current User By Username (String Principal)")
        void getCurrentUser_Case16_ByUsername() {
            try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
                // GIVEN
                Authentication auth = mock(Authentication.class);
                SecurityContext context = mock(SecurityContext.class);
                when(context.getAuthentication()).thenReturn(auth);
                when(auth.isAuthenticated()).thenReturn(true);
                when(auth.getPrincipal()).thenReturn(USERNAME); // Return String
                when(auth.getName()).thenReturn(USERNAME);
                mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);

                SysUser dbUser = new SysUser();
                dbUser.setId(USER_ID);
                when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(dbUser));

                OutstandingItem item = new OutstandingItem();
                item.setId(OUTSTANDING_ID);
                when(outstandingItemRepository.findByIdAndIsDeletedFalse(OUTSTANDING_ID)).thenReturn(Optional.of(item));

                OutstandingAcceptanceDto request = OutstandingAcceptanceDto.builder()
                        .result(OutstandingAcceptanceResultEnum.ACCEPTED)
                        .acceptanceNote("Note")
                        .build();
                when(mapper.toEntity(any())).thenReturn(new OutstandingAcceptance());
                when(mapper.toDtoList(anyList())).thenReturn(List.of(request));

                // WHEN
                service.create(OUTSTANDING_ID, request, null);

                // THEN
                verify(repository).save(acceptanceCaptor.capture());
                assertEquals(USER_ID, acceptanceCaptor.getValue().getAcceptedBy());
            }
        }
    }

    // ==================================================================================
    // PRIVATE HELPERS
    // ==================================================================================

    private void setupSecurityContext(MockedStatic<SecurityContextHolder> mockedSecurity, Long userId) {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        SysUser user = new SysUser();
        user.setId(userId);

        when(context.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(user);

        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
    }
}