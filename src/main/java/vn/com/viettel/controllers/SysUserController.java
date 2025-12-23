package vn.com.viettel.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.SysUserDto;
import vn.com.viettel.services.SysUserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class SysUserController {

    private final SysUserService service;

    public SysUserController(SysUserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SysUserDto> create(@RequestBody SysUserDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<SysUserDto> update(@PathVariable("userId") Long userId, @RequestBody SysUserDto dto) {
        return ResponseEntity.ok(service.update(userId, dto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SysUserDto> getById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getById(userId));
    }

    @GetMapping
    public ResponseEntity<List<SysUserDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable("userId") Long userId) {
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
