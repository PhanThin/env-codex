package vn.com.viettel.controllers;



import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.SysOrgDto;
import vn.com.viettel.services.SysOrgService;

import java.util.List;

@Tag(name = "01. Đơn vị", description = "Các API liên quan đến quản lý đơn vị")
@RestController
@RequestMapping("/api/v1/orgs")
public class SysOrgController {

    private final SysOrgService service;

    public SysOrgController(SysOrgService service) {
        this.service = service;
    }

    @Hidden
    @PostMapping
    public ResponseEntity<SysOrgDto> create(@RequestBody SysOrgDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Hidden
    @PutMapping("/{orgId}")
    public ResponseEntity<SysOrgDto> update(@PathVariable("orgId") Long orgId, @RequestBody SysOrgDto dto) {
        return ResponseEntity.ok(service.update(orgId, dto));
    }

    @Hidden
    @GetMapping("/{orgId}")
    public ResponseEntity<SysOrgDto> getById(@PathVariable("orgId") Long orgId) {
        return ResponseEntity.ok(service.getById(orgId));
    }


    @GetMapping
    public ResponseEntity<List<SysOrgDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Hidden
    @DeleteMapping("/{orgId}")
    public ResponseEntity<Void> delete(@PathVariable("orgId") Long orgId) {
        service.delete(orgId);
        return ResponseEntity.noContent().build();
    }
}
