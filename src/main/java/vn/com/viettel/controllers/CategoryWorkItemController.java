package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CategoryWorkItemDto;
import vn.com.viettel.dto.CategoryWorkItemSearchRequest;
import vn.com.viettel.services.CategoryWorkItemService;

import java.util.List;

@Tag(name = "14. Quản lý hạng mục công việc", description = "API quản lý hạng mục công việc")
@RestController
@RequestMapping("/api/v1/category-work-items")
@Slf4j
public class CategoryWorkItemController {

    @Autowired
    private CategoryWorkItemService categoryWorkItemService;

    @GetMapping
    public Page<CategoryWorkItemDto> searchCategoryWorkItem(CategoryWorkItemSearchRequest request) {
        return categoryWorkItemService.searchCategoryWorkItem(request);
    }

    @Operation(summary = "Tạo mới hạng mục công việc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo mới thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryWorkItemDto.class))}),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Dữ liệu đầu vào không tồn tại"),
            @ApiResponse(responseCode = "409", description = "Mã/tên hạng mục công việc đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping
    public ResponseEntity<CategoryWorkItemDto> createCategoryWorkItem(@RequestBody CategoryWorkItemDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryWorkItemService.createCategoryWorkItem(dto));
    }

    @Operation(summary = "Cập nhật hạng mục công việc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryWorkItemDto.class))}),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Dữ liệu đầu vào không tồn tại"),
            @ApiResponse(responseCode = "409", description = "Mã/tên hạng mục công việc đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryWorkItemDto> updateCategoryWorkItem(@PathVariable Long id, @RequestBody CategoryWorkItemDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryWorkItemService.updateCategoryWorkItem(id, dto));
    }

    @Operation(summary = "Xóa hạng mục công việc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Dữ liệu đầu vào không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @DeleteMapping
    public ResponseEntity<?> deleteCategoryWorkItem(@RequestBody List<Long> ids) {
        categoryWorkItemService.deleteCategoryWorkItems(ids);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Lấy thông tin hạng mục công việc theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryWorkItemDto.class))}),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Dữ liệu đầu vào không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryWorkItemDto> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryWorkItemService.getById(id));
    }

    @Operation(summary = "Tìm kiếm hạng mục công việc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryWorkItemDto.class))}),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Dữ liệu đầu vào không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping("/search")
    public ResponseEntity<Page<CategoryWorkItemDto>> search(@RequestBody CategoryWorkItemSearchRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryWorkItemService.searchCategoryWorkItem(request));
    }
}
