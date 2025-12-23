
package vn.com.viettel.controllers;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.services.OutstandingAcceptanceService;

/**
 * REST controller for OUTSTANDING_ACCEPTANCE.
 */
@RestController
@RequestMapping("/api/outstandings-acceptance/{outstandingId}")
@RequiredArgsConstructor
public class OutstandingAcceptanceController {

    private final OutstandingAcceptanceService service;

    @Operation(summary = "Create acceptance for outstanding")
    @PostMapping
    public ResponseEntity<OutstandingAcceptanceDto> create(
            @PathVariable Long outstandingId,
            @Valid @RequestBody OutstandingAcceptanceDto request) {
        return ResponseEntity.ok(service.create(outstandingId, request));
    }

    @Operation(summary = "Update acceptance")
    @PutMapping("/{acceptanceId}")
    public ResponseEntity<OutstandingAcceptanceDto> update(
            @PathVariable Long outstandingId,
            @PathVariable Long acceptanceId,
            @Valid @RequestBody OutstandingAcceptanceDto request) {
        return ResponseEntity.ok(service.update(outstandingId, acceptanceId, request));
    }

    @Operation(summary = "Get acceptance of outstanding")
    @GetMapping
    public ResponseEntity<OutstandingAcceptanceDto> get(@PathVariable Long outstandingId) {
        return ResponseEntity.ok(service.get(outstandingId));
    }

    @Operation(summary = "Soft delete acceptance")
    @DeleteMapping("/{acceptanceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long outstandingId,
            @PathVariable Long acceptanceId) {
        service.delete(outstandingId, acceptanceId);
        return ResponseEntity.noContent().build();
    }
}
