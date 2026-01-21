package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatManufacturerDto;
import vn.com.viettel.dto.CatSurveyEquipmentDto;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.dto.ProjectTypeDto;
import vn.com.viettel.services.CatSurveyEquipmentService;
import vn.com.viettel.utils.exceptions.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


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

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        List<CatSurveyEquipmentDto> data = service.getAll(sortBy, sortDir);
        return ResponseEntity.ok(data);
    }

    @Operation(
            summary = "Lấy chi tiết thiết bị khảo sát",
            description = "Trả về thông tin chi tiết của 1 thiết bị theo equipmentId (chỉ lấy bản ghi chưa bị xóa)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Thiếu/không hợp lệ equipmentId"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thiết bị")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CatSurveyEquipmentDto> getDetail(
            @Parameter(description = "ID thiết bị", required = true, example = "1")
            @PathVariable("id") Long id
    ) throws CustomException {
        return ResponseEntity.ok(service.getDetail(id));
    }

}
