package vn.com.viettel.controllers;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.SysOrgDto;
import vn.com.viettel.services.SysOrgService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orgs")
public class SysOrgController {

    private final SysOrgService service;

    public SysOrgController(SysOrgService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SysOrgDto> create(@RequestBody SysOrgDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{orgId}")
    public ResponseEntity<SysOrgDto> update(@PathVariable("orgId") Long orgId, @RequestBody SysOrgDto dto) {
        return ResponseEntity.ok(service.update(orgId, dto));
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<SysOrgDto> getById(@PathVariable("orgId") Long orgId) {
        return ResponseEntity.ok(service.getById(orgId));
    }

    @GetMapping
    public ResponseEntity<List<SysOrgDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{orgId}")
    public ResponseEntity<Void> delete(@PathVariable("orgId") Long orgId) {
        service.delete(orgId);
        return ResponseEntity.noContent().build();
    }
}
