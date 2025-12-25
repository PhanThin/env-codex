package vn.com.viettel.services.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.OutstandingItemMapper;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutstandingItemServiceImplTest {

    @InjectMocks
    private OutstandingItemServiceImpl service;

    @Mock private OutstandingItemRepository outstandingItemRepository;
    @Mock private OutstandingAlertConfigRepository outstandingAlertConfigRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectItemRepository projectItemRepository;
    @Mock private WorkItemRepository workItemRepository;
    @Mock private CatOutstandingTypeRepository outstandingTypeRepository;
    @Mock private SysUserRepository userRepository;
    @Mock private OutstandingAcceptanceRepository acceptanceRepository;
    @Mock private OutstandingProcessLogRepository processLogRepository;
    @Mock private Translator translator;
    @Mock private OutstandingItemMapper outstandingItemMapper;
    @Mock private AttachmentService attachmentService;

    // Static Mocks
    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    private MockedStatic<LocalDate> localDateMock;
    private MockedStatic<LocalDateTime> localDateTimeMock;
    private MockedStatic<OutstandingItemSpecifications> specificationsMock;

    // Dummy Data
    private OutstandingItemDto requestDto;
    private final Long VALID_ID = 1L;
    private final String VALID_TITLE = "Valid Title";
    private final LocalDate FUTURE_DATE = LocalDate.of(2025, 12, 31);
    private final LocalDate CURRENT_DATE = LocalDate.of(2025, 1, 1);
    private final LocalDateTime CURRENT_DATETIME = LocalDateTime.of(2025, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");

        // Init Valid DTO
        requestDto = new OutstandingItemDto();
        requestDto.setOutstandingTitle(VALID_TITLE);

        OutstandingTypeDto typeDto = new OutstandingTypeDto();
        typeDto.setId(10L);
        requestDto.setOutstandingTypeDto(typeDto);

        PriorityDto priorityDto = new PriorityDto();
        priorityDto.setCode("HIGH_PRIORITY");
        requestDto.setPriorityDto(priorityDto);

        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(100L);
        requestDto.setProjectDto(projectDto);

        ProjectItemDto itemDto = new ProjectItemDto();
        itemDto.setId(200L);
        requestDto.setProjectItemDto(itemDto);

        WorkItemDto workItemDto = new WorkItemDto();
        workItemDto.setId(300L);
        requestDto.setWorkItemDto(workItemDto);

        UserDto userDto = new UserDto();
        userDto.setId(999L);
        requestDto.setAssignedUserDto(userDto);

        OrgDto orgDto = new OrgDto();
        orgDto.setId(888L);
        requestDto.setAssignedOrgDto(orgDto);

        requestDto.setDeadline(FUTURE_DATE);

        AcceptanceTypeDto accTypeDto = new AcceptanceTypeDto();
        accTypeDto.setCode("WORK_ACCEPTANCE");
        requestDto.setAcceptanceType(accTypeDto);

        // Setup common translator mocks to avoid NPE in assertion
        // Case 1: getMessage(code)
        lenient().when(translator.getMessage(anyString())).thenAnswer(i -> i.getArguments()[0]);

        // Case 2: getMessage(code, arg1)
        lenient().when(translator.getMessage(anyString(), any())).thenAnswer(i -> i.getArguments()[0]);

        // Case 3: getMessage(code, arg1, arg2)
        lenient().when(translator.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArguments()[0]);
    }

    // --- GROUP 1: CREATE VALIDATION ---

    @Test
    @DisplayName("create_NullDto_ThrowsEx")
    void create_NullDto_ThrowsEx() {
        assertThrows(CustomException.class, () ->
            service.createOutstandingItem(null, null, null));
    }

    @Test
    @DisplayName("create_TitleNull_ThrowsEx")
    void create_TitleNull_ThrowsEx() {
        requestDto.setOutstandingTitle(null);
        CustomException ex = assertThrows(CustomException.class, () ->
            service.createOutstandingItem(requestDto, null, null));
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("outstanding.title.required", ex.getMessage());
    }

    @Test
    @DisplayName("create_TitleDuplicate_ThrowsEx")
    void create_TitleDuplicate_ThrowsEx() {
        when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(VALID_TITLE))
                .thenReturn(Optional.of(new OutstandingItem()));

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertEquals(HttpStatus.CONFLICT.value(), ex.getCodeError());
    }

    @Test
    @DisplayName("create_TypeNotFound_ThrowsEx")
    void create_TypeNotFound_ThrowsEx() {
        when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(outstandingTypeRepository.existsById(anyLong())).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        assertEquals("outstanding.type.notfound", ex.getMessage());
    }

    @Test
    @DisplayName("create_PriorityInvalid_ThrowsEx")
    void create_PriorityInvalid_ThrowsEx() {
        // Bypass previous checks
        when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(outstandingTypeRepository.existsById(anyLong())).thenReturn(true);

        requestDto.getPriorityDto().setCode("INVALID_CODE");

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("outstanding.priority.invalid", ex.getMessage());
    }

    @Test
    @DisplayName("create_ItemNotBelongToProject_ThrowsEx")
    void create_ItemNotBelongToProject_ThrowsEx() {
        // Setup Logic
        when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(outstandingTypeRepository.existsById(anyLong())).thenReturn(true);
        when(projectRepository.existsById(anyLong())).thenReturn(true);

        ProjectItem item = new ProjectItem();
        item.setId(200L);
        item.setProjectId(999L); // Different from requestDto(100L)
        when(projectItemRepository.findById(200L)).thenReturn(Optional.of(item));

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
    }

    @Test
    @DisplayName("create_DeadlinePast_ThrowsEx")
    void create_DeadlinePast_ThrowsEx() {
        // Setup Logic
        when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(outstandingTypeRepository.existsById(anyLong())).thenReturn(true);
        when(projectRepository.existsById(anyLong())).thenReturn(true);

        ProjectItem item = new ProjectItem();
        item.setProjectId(100L);
        when(projectItemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        WorkItem workItem = new WorkItem();
        workItem.setItemId(200L);
        when(workItemRepository.findById(anyLong())).thenReturn(Optional.of(workItem));

        // Mock Date to ensure deadline (FUTURE_DATE) is considered past relative to "now"
        LocalDate farFuture = FUTURE_DATE.plusDays(1);
        localDateMock = mockStatic(LocalDate.class);
        localDateMock.when(LocalDate::now).thenReturn(farFuture);

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("outstanding.deadline.past", ex.getMessage());

        localDateMock.close(); // Clean up immediately
    }

    // --- GROUP 2: ALERT CONFIG VALIDATION ---

    @Test
    @DisplayName("create_AlertDateBeforeDeadline_ThrowsEx")
    void create_AlertDateBeforeDeadline_ThrowsEx() {
        // Basic Mocks
        setupHappyPathValidationMocks();

        // Mock Date
        localDateMock = mockStatic(LocalDate.class);
        localDateMock.when(LocalDate::now).thenReturn(CURRENT_DATE);

        List<OutstandingAlertConfigDto> configs = new ArrayList<>();
        OutstandingAlertConfigDto config = new OutstandingAlertConfigDto();
        config.setLevelNo(1);
        config.setAlertDate(FUTURE_DATE.minusDays(1)); // Valid relative to now, but...
        // Logic check: "config.getAlertDate().isBefore(deadline)" ???
        // Wait, logic in service says: if (hasInvalidAlertDate) ... config.getAlertDate().isBefore(deadline)
        // IF deadline is FUTURE_DATE. Alert Date MUST be >= Deadline? Or is it warning before deadline?
        // Re-reading code: "config.getAlertDate() == null || config.getAlertDate().isBefore(deadline)"
        // So alert date MUST be >= deadline? This seems odd for "Alert", but based on code:
        // if (isBefore(deadline)) -> throw. So AlertDate must be AFTER or EQUAL deadline.

        config.setAlertDate(FUTURE_DATE.minusDays(1)); // Before deadline
        configs.add(config);
        requestDto.setOutstandingAlertConfigs(configs);

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertEquals("outstanding.alert.date.invalid", ex.getMessage());

        localDateMock.close();
    }

    @Test
    @DisplayName("create_AlertLevelsUnordered_ThrowsEx")
    void create_AlertLevelsUnordered_ThrowsEx() {
        setupHappyPathValidationMocks();
        localDateMock = mockStatic(LocalDate.class);
        localDateMock.when(LocalDate::now).thenReturn(CURRENT_DATE);

        List<OutstandingAlertConfigDto> configs = new ArrayList<>();

        OutstandingAlertConfigDto c1 = new OutstandingAlertConfigDto();
        c1.setLevelNo(1);
        c1.setAlertDate(FUTURE_DATE.plusDays(1));

        OutstandingAlertConfigDto c2 = new OutstandingAlertConfigDto();
        c2.setLevelNo(2);
        c2.setAlertDate(FUTURE_DATE.plusDays(1)); // Equal to level 1, not AFTER

        configs.add(c1);
        configs.add(c2);
        requestDto.setOutstandingAlertConfigs(configs);

        CustomException ex = assertThrows(CustomException.class, () ->
                service.createOutstandingItem(requestDto, null, null));
        assertTrue(ex.getMessage().contains("outstanding.alert.date.level.invalid"));

        localDateMock.close();
    }

    // --- GROUP 3: CREATE HAPPY PATH ---

    @Test
    @DisplayName("create_ValidInput_Success")
    void create_ValidInput_Success() {
        // 1. Setup Validation Mocks
        setupHappyPathValidationMocks();

        // 2. Mock Static Date & Security
        localDateMock = mockStatic(LocalDate.class);
        localDateMock.when(LocalDate::now).thenReturn(CURRENT_DATE);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        SysUser sysUser = new SysUser();
        sysUser.setId(777L);

        when(context.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(sysUser);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(context);

        // 3. Mock Saving Logic
        OutstandingItem mappedEntity = new OutstandingItem();
        mappedEntity.setId(VALID_ID);
        when(outstandingItemMapper.toEntity(any(), any())).thenReturn(mappedEntity);
        when(outstandingItemRepository.save(any(OutstandingItem.class))).thenReturn(mappedEntity);

        // Mock Mapper Return
        OutstandingItemDto resultDto = new OutstandingItemDto();
        resultDto.setId(VALID_ID);
        when(outstandingItemMapper.mapToOutstandingItemDto(anyList())).thenReturn(List.of(resultDto));

        // 4. Execution
        MultipartFile[] files = new MultipartFile[0];
        OutstandingItemDto result = service.createOutstandingItem(requestDto, files, files);

        // 5. Verification
        assertNotNull(result);
        assertEquals(VALID_ID, result.getId());

        // Verify Save Captor
        ArgumentCaptor<OutstandingItem> itemCaptor = ArgumentCaptor.forClass(OutstandingItem.class);
        verify(outstandingItemRepository).save(itemCaptor.capture());
        // Since we mocked toEntity, we rely on mapper correctness, but we verify save was called.

        // Verify Attachment
        verify(attachmentService, times(2)).handleAttachment(any(), eq(VALID_ID), anyString(), anyString());

        // Cleanup
        localDateMock.close();
        securityContextHolderMock.close();
    }

    // --- GROUP 4: UPDATE ---

    @Test
    @DisplayName("update_IdNull_ThrowsEx")
    void update_IdNull_ThrowsEx() {
        assertThrows(CustomException.class, () ->
            service.updateOutstandingItem(null, requestDto, null, null));
    }

    @Test
    @DisplayName("update_NotFound_ThrowsEx")
    void update_NotFound_ThrowsEx() {
        // Pass validations
        setupHappyPathValidationMocks();
        localDateMock = mockStatic(LocalDate.class);
        localDateMock.when(LocalDate::now).thenReturn(CURRENT_DATE);

        // Mock Not Found
        when(outstandingItemRepository.findByIdAndIsDeletedFalse(VALID_ID)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () ->
                service.updateOutstandingItem(VALID_ID, requestDto, null, null));
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());

        localDateMock.close();
    }

    @Test
    @DisplayName("update_TitleDuplicateWithOther_ThrowsEx")
    void update_TitleDuplicateWithOther_ThrowsEx() {
        OutstandingItem existingWithSameName = new OutstandingItem();
        existingWithSameName.setId(999L); // Different ID

        when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(VALID_TITLE))
                .thenReturn(Optional.of(existingWithSameName));

        requestDto.setId(VALID_ID); // Current ID

        CustomException ex = assertThrows(CustomException.class, () ->
                service.updateOutstandingItem(VALID_ID, requestDto, null, null));
        assertEquals(HttpStatus.CONFLICT.value(), ex.getCodeError());
    }

    @Test
    @DisplayName("update_ValidInput_UpdateConfigs_Success")
    void update_ValidInput_UpdateConfigs_Success() {
        // Setup
        setupHappyPathValidationMocks();
        localDateMock = mockStatic(LocalDate.class);
        localDateMock.when(LocalDate::now).thenReturn(CURRENT_DATE);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(context);

        // Mock Existing Item
        OutstandingItem existingItem = new OutstandingItem();
        existingItem.setId(VALID_ID);
        when(outstandingItemRepository.findByIdAndIsDeletedFalse(VALID_ID)).thenReturn(Optional.of(existingItem));
        when(outstandingItemRepository.save(any())).thenReturn(existingItem);

        // Mock Alert Configs Logic
        // Old Configs in DB: Level 1
        OutstandingAlertConfig oldConfig = new OutstandingAlertConfig();
        oldConfig.setLevelNo(1);
        oldConfig.setId(10L);
        when(outstandingAlertConfigRepository.findByOutstandingIdAndIsDeletedFalse(VALID_ID))
                .thenReturn(List.of(oldConfig));

        // New Configs in DTO: Level 1 (Update), Level 2 (Insert)
        OutstandingAlertConfigDto c1 = new OutstandingAlertConfigDto();
        c1.setLevelNo(1);
        c1.setAlertDate(FUTURE_DATE.plusDays(5));

        OutstandingAlertConfigDto c2 = new OutstandingAlertConfigDto();
        c2.setLevelNo(2);
        c2.setAlertDate(FUTURE_DATE.plusDays(10));

        requestDto.setOutstandingAlertConfigs(List.of(c1, c2));

        // Mock Mapper for Configs
        OutstandingAlertConfig mappedC1 = new OutstandingAlertConfig(); mappedC1.setLevelNo(1); mappedC1.setPercentTime(BigDecimal.valueOf(50.0));
        OutstandingAlertConfig mappedC2 = new OutstandingAlertConfig(); mappedC2.setLevelNo(2);
        when(outstandingItemMapper.mapToOutstandingAlertConfig(anyList(), anyLong())).thenReturn(List.of(mappedC1, mappedC2));

        when(outstandingItemMapper.mapToOutstandingItemDto(anyList())).thenReturn(List.of(new OutstandingItemDto()));

        // Execute
        service.updateOutstandingItem(VALID_ID, requestDto, null, null);

        // Verify
        // 1. Alert Config Level 1 should be UPDATED (save called on existing object)
        verify(outstandingAlertConfigRepository).save(oldConfig);
        // 2. Alert Config Level 2 should be INSERTED
        verify(outstandingAlertConfigRepository).save(mappedC2);

        // Cleanup
        localDateMock.close();
        securityContextHolderMock.close();
    }

    // --- GROUP 5: DELETE ---

    @Test
    @DisplayName("delete_StatusInvalid_ThrowsEx")
    void delete_StatusInvalid_ThrowsEx() {
        OutstandingItem item = new OutstandingItem();
        item.setId(VALID_ID);
        item.setStatus("CLOSED"); // Invalid status for delete

        when(outstandingItemRepository.findAllByIdInAndIsDeletedFalse(anyList()))
                .thenReturn(List.of(item));

        CustomException ex = assertThrows(CustomException.class, () ->
                service.deleteOutstandingItem(List.of(VALID_ID)));
        assertEquals("outstandingitem.status.invalid", ex.getMessage());
    }

    @Test
    @DisplayName("delete_Valid_Success")
    void delete_Valid_Success() {
        // Setup
        OutstandingItem item = new OutstandingItem();
        item.setId(VALID_ID);
        item.setStatus("NEW");

        when(outstandingItemRepository.findAllByIdInAndIsDeletedFalse(anyList()))
                .thenReturn(List.of(item));

        localDateTimeMock = mockStatic(LocalDateTime.class);
        localDateTimeMock.when(LocalDateTime::now).thenReturn(CURRENT_DATETIME);

        // Execute
        service.deleteOutstandingItem(List.of(VALID_ID));

        // Verify Item Soft Delete
        ArgumentCaptor<List<OutstandingItem>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(outstandingItemRepository).saveAll(itemCaptor.capture());
        assertTrue(itemCaptor.getValue().getFirst().getIsDeleted());
        assertEquals(CURRENT_DATETIME, itemCaptor.getValue().getFirst().getLastUpdateAt());

        // Verify Related Data Soft Delete
        verify(outstandingAlertConfigRepository).findAllByOutstandingIdInAndIsDeletedFalse(anyList());
        verify(processLogRepository).findAllByOutstandingIdInAndIsDeletedFalse(anyList());
        verify(acceptanceRepository).findAllByOutstandingIdInAndIsDeletedFalse(anyList());
        verify(attachmentService).deleteAttachments(anyList(), eq(Constants.OUTSTANDING_REFERENCE_TYPE), anyLong());

        localDateTimeMock.close();
    }

    // --- GROUP 6: GET BY ID ---

    @Test
    @DisplayName("getById_Success")
    void getById_Success() {
        OutstandingItem item = new OutstandingItem();
        when(outstandingItemRepository.findByIdAndIsDeletedFalse(VALID_ID)).thenReturn(Optional.of(item));
        when(outstandingItemMapper.mapToOutstandingItemDto(anyList())).thenReturn(List.of(requestDto));

        OutstandingItemDto result = service.getOutstandingItemById(VALID_ID);
        assertNotNull(result);
    }

    // --- GROUP 7: SEARCH ---

    @Test
    @DisplayName("search_CustomSort_Status_Success")
    void search_CustomSort_Status_Success() {
        OutstandingItemSearchRequestDto req = new OutstandingItemSearchRequestDto();
        req.setSortBy("status");
        req.setSortDirection("ASC");

        specificationsMock = mockStatic(OutstandingItemSpecifications.class);
        Specification<OutstandingItem> spec = mock(Specification.class);
        specificationsMock.when(() -> OutstandingItemSpecifications.fromRecommendationSearch(req)).thenReturn(spec);
        specificationsMock.when(() -> OutstandingItemSpecifications.withCustomSort("status", Sort.Direction.ASC)).thenReturn(mock(Specification.class));

        when(spec.and(any())).thenReturn(spec);

        Page<OutstandingItem> pageEntity = new PageImpl<>(List.of(new OutstandingItem()));
        when(outstandingItemRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageEntity);

        service.searchOutstanding(req);

        // Verify Custom Sort Spec called
        specificationsMock.verify(() -> OutstandingItemSpecifications.withCustomSort("status", Sort.Direction.ASC));
        specificationsMock.close();
    }

    @Test
    @DisplayName("search_StandardSort_Success")
    void search_StandardSort_Success() {
        OutstandingItemSearchRequestDto req = new OutstandingItemSearchRequestDto();
        req.setSortBy("createdAt"); // Mapped to "createdAt"
        req.setSortDirection("DESC");

        specificationsMock = mockStatic(OutstandingItemSpecifications.class);
        Specification<OutstandingItem> spec = mock(Specification.class);
        specificationsMock.when(() -> OutstandingItemSpecifications.fromRecommendationSearch(req)).thenReturn(spec);

        Page<OutstandingItem> pageEntity = new PageImpl<>(List.of(new OutstandingItem()));
        when(outstandingItemRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageEntity);

        service.searchOutstanding(req);

        // Verify PageRequest created with Sort
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(outstandingItemRepository).findAll(eq(spec), pageRequestCaptor.capture());

        Sort sort = pageRequestCaptor.getValue().getSort();
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("createdAt").getDirection());

        specificationsMock.close();
    }

    // --- HELPERS ---

    private void setupHappyPathValidationMocks() {
        lenient().when(outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(anyString())).thenReturn(Optional.empty());
        lenient().when(outstandingTypeRepository.existsById(anyLong())).thenReturn(true);
        lenient().when(projectRepository.existsById(anyLong())).thenReturn(true);

        ProjectItem item = new ProjectItem();
        item.setProjectId(100L);
        lenient().when(projectItemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        WorkItem workItem = new WorkItem();
        workItem.setItemId(200L);
        lenient().when(workItemRepository.findById(anyLong())).thenReturn(Optional.of(workItem));
    }
}