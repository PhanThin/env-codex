package vn.com.viettel.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatSurveyEquipmentDto;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.services.CatSurveyEquipmentService;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/survey-equipments")
public class CatSurveyEquipmentController {

    @Autowired
    private CatSurveyEquipmentService service;

    @PostMapping
    public CatSurveyEquipmentDto create(@RequestBody CatSurveyEquipmentDto dto) throws CustomException {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public CatSurveyEquipmentDto update(@PathVariable("id") Long id, @RequestBody CatSurveyEquipmentDto dto) throws CustomException {
        return service.update(id, dto);
    }

    @DeleteMapping
    public void delete(@RequestBody List<Long> equipmentIds) throws CustomException {
        service.delete(equipmentIds);
    }

    @GetMapping("/search")
    public Page<CatSurveyEquipmentDto> search(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            @RequestParam(value = "createdAtFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtTo,
            @RequestParam(value = "equipmentCode", required = false) String equipmentCode,
            @RequestParam(value = "equipmentName", required = false) String equipmentName,
            @RequestParam(value = "manageUnitId", required = false) Long manageUnitId,
            @RequestParam(value = "isActive", required = false) String isActive
    ) throws CustomException {
        CatSurveyEquipmentSearchRequestDto req = new CatSurveyEquipmentSearchRequestDto();
        req.setPage(page);
        req.setSize(size);
        req.setSortBy(sortBy);
        req.setSortDirection(sortDirection);
        req.setCreatedAtFrom(createdAtFrom);
        req.setCreatedAtTo(createdAtTo);
        req.setEquipmentCode(equipmentCode);
        req.setEquipmentName(equipmentName);
        req.setManageUnitId(manageUnitId);
        req.setIsActive(isActive);
        return service.search(req);
    }
}
