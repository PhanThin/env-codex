package vn.com.viettel.controllers;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.SysUserDto;
import vn.com.viettel.services.SysUserService;

import java.util.List;

@Tag(name = "02. Quản lý người dùng", description = "API quản lý người dùng")
@RestController
@RequestMapping("/api/v1/users")
public class SysUserController {

    private final SysUserService service;

    public SysUserController(SysUserService service) {
        this.service = service;
    }

    @Hidden
    @PostMapping
    public ResponseEntity<SysUserDto> create(@RequestBody SysUserDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Hidden
    @PutMapping("/{userId}")
    public ResponseEntity<SysUserDto> update(@PathVariable("userId") Long userId, @RequestBody SysUserDto dto) {
        return ResponseEntity.ok(service.update(userId, dto));
    }

    @Hidden
    @GetMapping("/{userId}")
    public ResponseEntity<SysUserDto> getById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getById(userId));
    }

    @GetMapping
    public ResponseEntity<List<SysUserDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Hidden
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable("userId") Long userId) {
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
