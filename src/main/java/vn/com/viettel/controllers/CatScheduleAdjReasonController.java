package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatScheduleAdjReasonDto;
import vn.com.viettel.dto.CatScheduleAdjReasonSearchRequestDto;
import vn.com.viettel.services.CatScheduleAdjReasonService;

import java.util.List;

@Tag(name = "17. Lý do điều chỉnh tiến độ", description = "API quản lý danh mục lý do điều chỉnh tiến độ")
@RestController
@RequestMapping("/api/v1/cat-schedule-adj-reason")
@Validated
public class CatScheduleAdjReasonController {

    @Autowired
    private CatScheduleAdjReasonService service;

    @Operation(summary = "Tạo mới lý do điều chỉnh tiến độ")
    @PostMapping
    public ResponseEntity<CatScheduleAdjReasonDto> create(@Parameter(description = "Thông tin lý do", required = true)
                                                          @Valid @RequestBody CatScheduleAdjReasonDto dto) {
        CatScheduleAdjReasonDto result = service.create(dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Cập nhật lý do điều chỉnh tiến độ")
    @PutMapping("/{id}")
    public ResponseEntity<CatScheduleAdjReasonDto> update(@Parameter(description = "ID lý do", required = true) @PathVariable("id") Long id,
                                                          @Parameter(description = "Thông tin cập nhật", required = true)
                                                          @Valid @RequestBody CatScheduleAdjReasonDto dto) {
        CatScheduleAdjReasonDto result = service.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Xóa (soft delete) nhiều lý do điều chỉnh tiến độ")
    @DeleteMapping
    public ResponseEntity<?> delete(@Parameter(description = "Danh sách ID", required = true) @RequestBody List<Long> reasonIds) {
        service.delete(reasonIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Lấy chi tiết lý do điều chỉnh tiến độ")
    @GetMapping("/{id}")
    public ResponseEntity<CatScheduleAdjReasonDto> getDetail(@Parameter(description = "ID lý do", required = true) @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getDetail(id));
    }

    @Operation(summary = "Tìm kiếm lý do điều chỉnh tiến độ")
    @GetMapping("/search")
    public ResponseEntity<Page<CatScheduleAdjReasonDto>> search(@Parameter(description = "Tiêu chí tìm kiếm") CatScheduleAdjReasonSearchRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(service.search(request));
    }
}
