package vn.com.viettel.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.RecommendationMapper;
import vn.com.viettel.minio.services.FileService;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Mock
    private Translator translator;

    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectItemRepository projectItemRepository;

    @Mock
    private CatProjectPhaseRepository phaseRepository;

    @Mock
    private CatRecommendationTypeRepository recommendationTypeRepository;

    @Mock
    private RecommendationWorkItemRepository recommendationWorkItemRepository;

    @Mock
    private RecommendationAssignmentRepository recommendationAssignmentRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private RecommendationResponseRepository recommendationResponseRepository;

    @Mock
    private SysUserRepository sysUserRepository;

    @Mock
    private FileService fileService;

    @Mock
    private AttachmentService attachmentService;

    // ========= EXISTING TESTS (updateRecommendation) =========

    @Test
    void updateRecommendation_success() throws CustomException {
        Long recommendationId = 1L;
        CatRecommendationTypeDto recommendationTypeDto = new CatRecommendationTypeDto();
        recommendationTypeDto.setId(1L);

        PriorityDto priorityDto = new PriorityDto();
        priorityDto.setCode("HIGH_PRIORITY");

        RecommendationDto inputDto = new RecommendationDto();
        inputDto.setRecommendationTitle("Updated Title");
        inputDto.setContent("Updated Content");
        inputDto.setRecommendationType(recommendationTypeDto);
        inputDto.setPriority(priorityDto);
        inputDto.setDeadline(LocalDate.now().plusDays(10));
        inputDto.setId(recommendationId);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        inputDto.setCurrentProcessUser(userDto);

        Recommendation recommendation = new Recommendation();
        recommendation.setId(recommendationId);
        recommendation.setStatus(RecommendationStatusEnum.NEW.name());

        when(sysUserRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(new SysUser()));
        when(recommendationRepository.findByIdAndIsDeletedFalse(recommendationId)).thenReturn(Optional.of(recommendation));
        when(recommendationTypeRepository.existsById(1L)).thenReturn(true);
        when(recommendationRepository.findByRecommendationTitle("Updated Title")).thenReturn(Optional.empty());
        when(recommendationMapper.mapToRecommendationWorkItem(anyList())).thenReturn(List.of(inputDto));

        RecommendationDto result = recommendationService.updateRecommendation(recommendationId, inputDto, null);

        assertNotNull(result);
        assertEquals("Updated Title", result.getRecommendationTitle());
        verify(recommendationRepository, times(1)).save(recommendation);
    }

    @Test
    void updateRecommendation_notFound() {
        Long recommendationId = 1L;
        RecommendationDto inputDto = new RecommendationDto();

        when(recommendationRepository.findByIdAndIsDeletedFalse(recommendationId)).thenReturn(Optional.empty());
        when(translator.getMessage("recommendation.notFound", recommendationId)).thenReturn("Recommendation not found.");

        CustomException exception = assertThrows(CustomException.class,
                () -> recommendationService.updateRecommendation(recommendationId, inputDto, null));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getCodeError());
        assertEquals("Recommendation not found.", exception.getMessage());
    }

    @Test
    void updateRecommendation_missingId() {
        RecommendationDto inputDto = new RecommendationDto();

        when(translator.getMessage("recommendation.id.null")).thenReturn("Recommendation ID is required.");

        CustomException exception = assertThrows(CustomException.class,
                () -> recommendationService.updateRecommendation(null, inputDto, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCodeError());
        assertEquals("Recommendation ID is required.", exception.getMessage());
    }

    @Test
    void updateRecommendation_duplicateTitle() {
        Long recommendationId = 1L;
        RecommendationDto inputDto = new RecommendationDto();
        inputDto.setId(recommendationId);
        inputDto.setRecommendationTitle("Duplicate Title");
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        inputDto.setCurrentProcessUser(userDto);

        Recommendation existingRecommendation = new Recommendation();
        existingRecommendation.setId(2L);
        existingRecommendation.setRecommendationTitle("Duplicate Title");
        existingRecommendation.setStatus(RecommendationStatusEnum.NEW.name());

        when(recommendationRepository.findByIdAndIsDeletedFalse(recommendationId))
                .thenReturn(Optional.of(existingRecommendation));
        when(recommendationRepository.findByRecommendationTitle("Duplicate Title"))
                .thenReturn(Optional.of(existingRecommendation));
        when(translator.getMessage("recommendation.title.duplicate", "Duplicate Title"))
                .thenReturn("Recommendation title already exists.");

        CustomException exception = assertThrows(CustomException.class,
                () -> recommendationService.updateRecommendation(recommendationId, inputDto, null));

        assertEquals(HttpStatus.CONFLICT.value(), exception.getCodeError());
        assertEquals("Recommendation title already exists.", exception.getMessage());
    }

    @Test
    void updateRecommendation_invalidPriority() {
        Long recommendationId = 1L;
        PriorityDto priorityDto = new PriorityDto();
        priorityDto.setCode("INVALID_PRIORITY");

        CatRecommendationTypeDto recommendationTypeDto = new CatRecommendationTypeDto();
        recommendationTypeDto.setId(1L);

        RecommendationDto inputDto = new RecommendationDto();
        inputDto.setRecommendationTitle("Title");
        inputDto.setContent("Content");
        inputDto.setPriority(priorityDto);
        inputDto.setRecommendationType(recommendationTypeDto);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        inputDto.setCurrentProcessUser(userDto);

        Recommendation existingRecommendation = new Recommendation();
        existingRecommendation.setId(recommendationId);
        existingRecommendation.setStatus(RecommendationStatusEnum.NEW.name());


//        when(sysUserRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(new SysUser()));
        when(recommendationRepository.findByIdAndIsDeletedFalse(recommendationId)).thenReturn(Optional.of(existingRecommendation));
        when(recommendationTypeRepository.existsById(1L)).thenReturn(true);
        when(translator.getMessage("recommendation.priority.invalid")).thenReturn("Invalid priority level.");

        CustomException exception = assertThrows(CustomException.class,
                () -> recommendationService.updateRecommendation(recommendationId, inputDto, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCodeError());
        assertEquals("Invalid priority level.", exception.getMessage());
    }

    // ========= NEW TESTS: createRecommendation =========

    @Test
    void createRecommendation_success() {
        RecommendationDto dto = new RecommendationDto();
        dto.setRecommendationTitle("New Title");
        dto.setContent("New Content");

        CatRecommendationTypeDto recommendationTypeDto = new CatRecommendationTypeDto();
        recommendationTypeDto.setId(1L);
        dto.setRecommendationType(recommendationTypeDto);

        PriorityDto priorityDto = new PriorityDto();
        priorityDto.setCode("HIGH_PRIORITY");
        dto.setPriority(priorityDto);

        UserDto currentUser = new UserDto();
        currentUser.setId(1L);
        dto.setCurrentProcessUser(currentUser);

        Recommendation entity = new Recommendation();
        entity.setId(1L);

        when(sysUserRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(new SysUser()));
        when(recommendationTypeRepository.existsById(1L)).thenReturn(true);
        when(recommendationRepository.findByRecommendationTitle("New Title")).thenReturn(Optional.empty());
        when(recommendationMapper.toEntity(eq(dto), any())).thenReturn(entity);
        when(recommendationMapper.mapToRecommendationWorkItem(List.of(entity))).thenReturn(List.of(dto));

        RecommendationDto result = recommendationService.createRecommendation(dto, null);

        assertNotNull(result);
        assertEquals("New Title", result.getRecommendationTitle());
        verify(recommendationRepository).save(entity);
    }

    @Test
    void createRecommendation_payloadNull() {
        when(translator.getMessage("recommendation.payload.null")).thenReturn("payload null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.createRecommendation(null, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("payload null", ex.getMessage());
    }

    // ========= NEW TESTS: deleteRecommendations =========

    @Test
    void deleteRecommendations_idsNull() {
        when(translator.getMessage("recommendation.id.null")).thenReturn("ID is null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.deleteRecommendations(null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("ID is null", ex.getMessage());
    }

    @Test
    void deleteRecommendations_idsEmpty() {
        when(translator.getMessage("recommendation.id.null")).thenReturn("ID is null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.deleteRecommendations(List.of()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("ID is null", ex.getMessage());
    }

    @Test
    void deleteRecommendations_notFound() {
        List<Long> ids = List.of(1L, 2L);
        when(recommendationRepository.findAllByIdInAndIsDeletedFalse(ids)).thenReturn(List.of());
        when(translator.getMessage("recommendation.notFound", ids)).thenReturn("Not found");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.deleteRecommendations(ids));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void deleteRecommendations_success() {
        List<Long> ids = List.of(1L);
        Recommendation rec = new Recommendation();
        rec.setId(1L);
        rec.setIsDeleted(false);

        when(recommendationRepository.findAllByIdInAndIsDeletedFalse(ids)).thenReturn(List.of(rec));
        when(recommendationWorkItemRepository.findAllByRecommendationIdInAndIsDeletedFalse(ids)).thenReturn(List.of());
//        when(attachmentRepository.findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(eq(ids), anyString()))
//                .thenReturn(List.of());

        assertDoesNotThrow(() -> recommendationService.deleteRecommendations(ids));

        verify(recommendationRepository).saveAll(anyList());
    }

    // ========= NEW TESTS: getRecommendationById =========

    @Test
    void getRecommendationById_nullId() {
        when(translator.getMessage("recommendation.id.null")).thenReturn("ID null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.getRecommendationById(null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("ID null", ex.getMessage());
    }

    @Test
    void getRecommendationById_notFound() {
        Long id = 1L;
        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
        when(translator.getMessage("recommendation.notFound", id)).thenReturn("Not found");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.getRecommendationById(id));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void getRecommendationById_success() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        RecommendationDto dto = new RecommendationDto();
        dto.setId(id);

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(recommendationMapper.mapToRecommendationWorkItem(List.of(rec))).thenReturn(List.of(dto));

        RecommendationDto result = recommendationService.getRecommendationById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    // ========= NEW TESTS: searchRecommendations =========

    @Test
    void searchRecommendations_invalidSortBy() {
        RecommendationSearchRequestDto request = new RecommendationSearchRequestDto();
        request.setSortBy("invalid_field");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.searchRecommendations(request));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertTrue(ex.getMessage().contains("sortBy không hợp lệ"));
    }

    @Test
    void searchRecommendations_invalidSortDirection() {
        RecommendationSearchRequestDto request = new RecommendationSearchRequestDto();
        request.setSortDirection("ABC");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.searchRecommendations(request));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertTrue(ex.getMessage().contains("sortDirection không hợp lệ"));
    }

    @Test
    void searchRecommendations_success() {
        RecommendationSearchRequestDto request = new RecommendationSearchRequestDto();
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("createdAt");
        request.setSortDirection("DESC");

        Recommendation rec = new Recommendation();
        rec.setId(1L);
        Page<Recommendation> page = new PageImpl<>(List.of(rec));

        when(recommendationRepository.findAll(ArgumentMatchers.<Specification<Recommendation>>any(), any(Pageable.class))).thenReturn(page);
        RecommendationDto dto = new RecommendationDto();
        dto.setId(1L);
        when(recommendationMapper.mapToRecommendationWorkItem(anyList())).thenReturn(List.of(dto));

        Page<RecommendationDto> result = recommendationService.searchRecommendations(request);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().getId());
    }

    // ========= NEW TESTS: closeRecommendation =========

    @Test
    void closeRecommendation_nullId() {
        when(translator.getMessage("recommendation.id.null")).thenReturn("ID null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.closeRecommendation(null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("ID null", ex.getMessage());
    }

    @Test
    void closeRecommendation_notFound() {
        Long id = 1L;
        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
        when(translator.getMessage("recommendation.notFound", id)).thenReturn("Not found");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.closeRecommendation(id));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void closeRecommendation_alreadyDone() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.DONE.name());

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(translator.getMessage("recommendation.already_done")).thenReturn("Already done");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.closeRecommendation(id));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("Already done", ex.getMessage());
    }

    @Test
    void closeRecommendation_success() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.NEW.name());

        RecommendationDto dto = new RecommendationDto();
        dto.setId(id);

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(recommendationMapper.mapToRecommendationWorkItem(List.of(rec))).thenReturn(List.of(dto));

        RecommendationDto result = recommendationService.closeRecommendation(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(recommendationRepository).save(rec);
        assertEquals(RecommendationStatusEnum.DONE.name(), rec.getStatus());
        assertNotNull(rec.getClosedAt());
    }

    // ========= NEW TESTS: addRecommendationResponse =========

    @Test
    void addRecommendationResponse_nullId() {
        when(translator.getMessage("recommendation.id.null")).thenReturn("ID null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.addRecommendationResponse(null, new RecommendationResponseDto(), null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("ID null", ex.getMessage());
    }

    @Test
    void addRecommendationResponse_nullDto() {
        Long id = 1L;
        when(translator.getMessage("recommendation.payload.null")).thenReturn("response null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.addRecommendationResponse(id, null, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("response null", ex.getMessage());
    }

    @Test
    void addRecommendationResponse_notFoundRecommendation() {
        Long id = 1L;
        RecommendationResponseDto dto = new RecommendationResponseDto();
        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
        when(translator.getMessage("recommendation.notFound", id)).thenReturn("Not found");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.addRecommendationResponse(id, dto, null));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void addRecommendationResponse_alreadyDone() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.DONE.name());
        RecommendationResponseDto dto = new RecommendationResponseDto();

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(translator.getMessage("recommendation.already_done")).thenReturn("Already done");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.addRecommendationResponse(id, dto, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("Already done", ex.getMessage());
    }

    @Test
    void addRecommendationResponse_blankContent() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.NEW.name());
        RecommendationResponseDto dto = new RecommendationResponseDto();
        dto.setResponseContent("   ");

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(translator.getMessage("recommendation.response.content.required"))
                .thenReturn("Content required");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.addRecommendationResponse(id, dto, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("Content required", ex.getMessage());
    }

    @Test
    void addRecommendationResponse_contentTooLong() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.NEW.name());
        RecommendationResponseDto dto = new RecommendationResponseDto();
        dto.setResponseContent("x".repeat(501));

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(translator.getMessage("recommendation.response.content.length"))
                .thenReturn("Too long");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.addRecommendationResponse(id, dto, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("Too long", ex.getMessage());
    }

    @Test
    void addRecommendationResponse_success_withoutFiles() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.NEW.name());

        RecommendationResponseDto inputDto = new RecommendationResponseDto();
        inputDto.setResponseContent("OK");

        RecommendationResponse responseEntity = new RecommendationResponse();
        responseEntity.setId(10L);
        responseEntity.setRecommendationId(id);
        responseEntity.setRespondedAt(LocalDateTime.now());

        RecommendationResponseDto outputDto = new RecommendationResponseDto();
        outputDto.setRecommendationId(id);
        outputDto.setResponseContent("OK");

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(recommendationMapper.mapToRecommendationResponse(eq(inputDto), eq(id), any()))
                .thenReturn(responseEntity);
        when(recommendationMapper.mapToRecommendationResponseDto(eq(responseEntity), any(), anyList()))
                .thenReturn(outputDto);

        RecommendationResponseDto result =
                recommendationService.addRecommendationResponse(id, inputDto, null);

        assertNotNull(result);
        assertEquals(id, result.getRecommendationId());
        verify(recommendationResponseRepository).save(responseEntity);
        verify(fileService, never()).uploadFiles(anyString(), anyString(), any());
    }

    @Test
    void addRecommendationResponse_success_withFiles() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setStatus(RecommendationStatusEnum.NEW.name());

        RecommendationResponseDto inputDto = new RecommendationResponseDto();
        inputDto.setResponseContent("OK");

        RecommendationResponse responseEntity = new RecommendationResponse();
        responseEntity.setId(10L);
        responseEntity.setRecommendationId(id);
        responseEntity.setRespondedAt(LocalDateTime.now());

        RecommendationResponseDto outputDto = new RecommendationResponseDto();
        outputDto.setRecommendationId(id);
        outputDto.setResponseContent("OK");

        MultipartFile file = mock(MultipartFile.class);
        Attachment attachment = new Attachment();
        attachment.setId(1L);
        attachment.setReferenceId(responseEntity.getId());
        attachment.setReferenceType(Constants.RECOMMENDATION_RESPONSE_REFERENCE_TYPE);


        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(recommendationMapper.mapToRecommendationResponse(eq(inputDto), eq(id), any()))
                .thenReturn(responseEntity);
        when(attachmentService.handleAttachment(any(), anyLong(), anyString(), anyString()))
                .thenReturn(List.of(attachment));
        when(recommendationMapper.mapToRecommendationResponseDto(eq(responseEntity), any(), anyList()))
                .thenReturn(outputDto);

        RecommendationResponseDto result =
                recommendationService.addRecommendationResponse(id, inputDto, new MultipartFile[]{file});

        assertNotNull(result);
        assertEquals(id, result.getRecommendationId());
        verify(recommendationResponseRepository).save(responseEntity);
        verify(attachmentService).handleAttachment(any(), eq(responseEntity.getId()),
                eq(Constants.RECOMMENDATION_RESPONSE_REFERENCE_TYPE), contains(String.valueOf(responseEntity.getId())));

    }

    // ========= NEW TESTS: getRecommendationResponses =========

    @Test
    void getRecommendationResponses_nullId() {
        when(translator.getMessage("recommendation.id.null")).thenReturn("ID null");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.getRecommendationResponses(null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("ID null", ex.getMessage());
    }

    @Test
    void getRecommendationResponses_notFoundRecommendation() {
        Long id = 1L;
        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
        when(translator.getMessage("recommendation.notFound", id)).thenReturn("Not found");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.getRecommendationResponses(id));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getCodeError());
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void getRecommendationResponses_success() {
        Long id = 1L;
        Recommendation rec = new Recommendation();
        rec.setId(id);

        RecommendationResponse response = new RecommendationResponse();
        response.setId(10L);
        response.setRecommendationId(id);

        RecommendationResponseDto dto = new RecommendationResponseDto();
        dto.setRecommendationId(id);

        when(recommendationRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(rec));
        when(recommendationResponseRepository.findAllByRecommendationIdAndIsDeletedFalse(id))
                .thenReturn(List.of(response));
        when(recommendationMapper.mapToRecommendationResponseDto(List.of(response)))
                .thenReturn(List.of(dto));

        List<RecommendationResponseDto> result = recommendationService.getRecommendationResponses(id);

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst().getRecommendationId());
    }


    @Test
    void validate_titleBlank() {
        RecommendationDto dto = new RecommendationDto();
        dto.setRecommendationTitle("  "); // blank
        dto.setContent("Content");
        CatRecommendationTypeDto typeDto = new CatRecommendationTypeDto();
        typeDto.setId(1L);
        dto.setRecommendationType(typeDto);
        PriorityDto priority = new PriorityDto();
        priority.setCode("HIGH_PRIORITY");
        dto.setPriority(priority);

        when(translator.getMessage("recommendation.title.required"))
                .thenReturn("title required");

        CustomException ex = assertThrows(CustomException.class,
                () -> recommendationService.validate(dto, false));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getCodeError());
        assertEquals("title required", ex.getMessage());
    }

    @Test
    void createRecommendation_withWorkItemsAssignedUsersAndFiles() {
        RecommendationDto dto = new RecommendationDto();
        dto.setRecommendationTitle("New Title");
        dto.setContent("New Content");

        CatRecommendationTypeDto recommendationTypeDto = new CatRecommendationTypeDto();
        recommendationTypeDto.setId(1L);
        dto.setRecommendationType(recommendationTypeDto);

        PriorityDto priorityDto = new PriorityDto();
        priorityDto.setCode("HIGH_PRIORITY");
        dto.setPriority(priorityDto);

        // WorkItems
        WorkItemDto wiDto = new WorkItemDto();
        wiDto.setId(100L);
        dto.setWorkItems(List.of(wiDto));

        // Assigned users
        UserDto userDto = new UserDto();
        userDto.setId(200L);
        dto.setCurrentProcessUser(userDto);

        Recommendation entity = new Recommendation();
        entity.setId(1L);

        MultipartFile file = mock(MultipartFile.class);
        Attachment attachment = new Attachment();
        attachment.setId(1L);
        attachment.setReferenceId(entity.getId());
        attachment.setReferenceType(Constants.RECOMMENDATION_REFERENCE_TYPE);

        when(sysUserRepository.findByIdAndIsDeletedFalse(200L)).thenReturn(Optional.of(new SysUser()));
        when(recommendationTypeRepository.existsById(1L)).thenReturn(true);
        when(recommendationRepository.findByRecommendationTitle("New Title")).thenReturn(Optional.empty());
        when(recommendationMapper.toEntity(eq(dto), any())).thenReturn(entity);
        when(recommendationMapper.mapToRecommendationWorkItem(List.of(entity))).thenReturn(List.of(dto));
        when(recommendationMapper.mapToRecommendationWorkItem(eq(dto.getWorkItems()), eq(entity.getId()), anyLong()))
                .thenReturn(List.of(new RecommendationWorkItem()));
//        when(recommendationMapper.mapToRecommendationAssignment(eq(dto.getAssignedUsers()), eq(entity.getId())))
//                .thenReturn(List.of(new RecommendationAssignment()));
        when(attachmentService.handleAttachment(any(), eq(entity.getId()),
                eq(Constants.RECOMMENDATION_REFERENCE_TYPE), eq(Constants.RECOMMENDATION_REFERENCE_TYPE)))
                .thenReturn(List.of(attachment));

        RecommendationDto result = recommendationService.createRecommendation(dto, new MultipartFile[]{file});

        assertNotNull(result);
        verify(recommendationWorkItemRepository).saveAll(anyList());
//        verify(recommendationAssignmentRepository).saveAll(anyList());
        verify(attachmentService).handleAttachment(any(), eq(entity.getId()),
                eq(Constants.RECOMMENDATION_REFERENCE_TYPE), eq(Constants.RECOMMENDATION_REFERENCE_TYPE));

    }
}
