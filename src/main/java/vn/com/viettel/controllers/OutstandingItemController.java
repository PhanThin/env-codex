package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingItemDto;
import vn.com.viettel.dto.OutstandingItemSearchRequestDto;
import vn.com.viettel.services.OutstandingItemService;

import java.util.List;

@Tag(name = "11. Quản lý tồn tại", description = "API quản lý tồn tại")
@RestController
@RequestMapping("/api/v1/outstanding")
@Slf4j
public class OutstandingItemController {
    @Autowired
    private OutstandingItemService outstandingItemService;

    @Operation(summary = "Tạo mới tồn tại", description = "Tạo mới tồn tại đi kèm với các file đính kèm (nếu có)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo mới thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OutstandingItemDto.class))}),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "409", description = "Tiêu đề tồn tại đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutstandingItemDto> createOutstandingItem(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin tồn tại và danh sách file",
                    content = @Content(
                            encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
            @RequestPart(value = "dto") OutstandingItemDto dto,
            @RequestPart(value = "acceptanceFiles", required = false) MultipartFile[] acceptanceFiles,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        OutstandingItemDto message = outstandingItemService.createOutstandingItem(dto, acceptanceFiles, files);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @Operation(summary = "Tìm kiếm tồn tại", description = "Tìm kiếm và phân trang tồn tại theo các tiêu chí")
    @PostMapping("/search")
    public ResponseEntity<Page<OutstandingItemDto>> searchOutstandingItems(@RequestBody @Validated OutstandingItemSearchRequestDto request) {
        Page<OutstandingItemDto> pageResult = outstandingItemService.searchOutstanding(request);
        return ResponseEntity.ok(pageResult);
    }

    @Operation(summary = "Cập nhật tồn tại", description = "Cập nhật thông tin tồn tại, xử lý thêm/xóa công việc và file đính kèm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{id}")
    public ResponseEntity<OutstandingItemDto> updateOutstandingItem(
            @Parameter(description = "ID của tồn tại cần cập nhật") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin tồn tại",
                    content = @Content(
                            encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
            @RequestPart(value = "dto") OutstandingItemDto dto,
            @RequestPart(value = "acceptanceFiles", required = false) MultipartFile[] acceptanceFiles,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        OutstandingItemDto message = outstandingItemService.updateOutstandingItem(id, dto, acceptanceFiles, files);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @Operation(summary = "Xóa tồn tại", description = "Xóa mềm tồn tại bằng cách đánh dấu trường is_deleted")
    @DeleteMapping()
    public ResponseEntity<?> deleteOutstandingItem(@Parameter(description = "ID của các tồn tại cần xóa") @RequestBody List<Long> ids) {
        outstandingItemService.deleteOutstandingItem(ids);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Lấy chi tiết tồn tại", description = "Lấy thông tin chi tiết của một tồn tại theo ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<OutstandingItemDto> getOutstandingItem(@Parameter(description = "ID của tồn tại") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(outstandingItemService.getOutstandingItemById(id));
    }

}
