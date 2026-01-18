package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatSurveyEquipmentDto;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.services.CatSurveyEquipmentService;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

@Tag(name = "17. Danh mục thiết bị", description = "API quản lý danh mục thiết bị")
@RestController
@RequestMapping("/api/v1/survey-equipments")
public class CatSurveyEquipmentController {

    @Autowired
    private CatSurveyEquipmentService service;

    @PostMapping
    public ResponseEntity<CatSurveyEquipmentDto> create(@RequestBody CatSurveyEquipmentDto dto) throws CustomException {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatSurveyEquipmentDto> update(@PathVariable("id") Long id, @RequestBody CatSurveyEquipmentDto dto) throws CustomException {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody List<Long> equipmentIds) throws CustomException {
        service.delete(equipmentIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<CatSurveyEquipmentDto>> search(@RequestBody CatSurveyEquipmentSearchRequestDto req) throws CustomException {
        return ResponseEntity.ok(service.search(req));
    }
}
