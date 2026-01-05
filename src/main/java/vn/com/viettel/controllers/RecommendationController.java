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
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationResponseDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.services.RecommendationService;

import java.util.List;

@Tag(name = "09. Quản lý kiến nghị", description = "API quản lý kiến nghị")
@RestController
@RequestMapping("/api/v1/recommendation")
@Slf4j
public class RecommendationController {
    @Autowired
    private RecommendationService recommendService;

    @Operation(summary = "Tạo mới kiến nghị", description = "Tạo mới kiến nghị đi kèm với các file đính kèm (nếu có)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo mới thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RecommendationDto.class))}),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "409", description = "Tiêu đề kiến nghị đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendationDto> createRecommendation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin kiến nghị và danh sách file",
                    content = @Content(
                            encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
            @RequestPart(value = "dto") RecommendationDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        RecommendationDto message = recommendService.createRecommendation(dto, files);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @Operation(summary = "Tìm kiếm kiến nghị", description = "Tìm kiếm và phân trang kiến nghị theo các tiêu chí")
    @PostMapping("/search")
    public ResponseEntity<Page<RecommendationDto>> searchRecommendations(@RequestBody @Validated RecommendationSearchRequestDto request) {
        Page<RecommendationDto> pageResult = recommendService.searchRecommendations(request);
        return ResponseEntity.ok(pageResult);
    }

    @Operation(summary = "Cập nhật kiến nghị", description = "Cập nhật thông tin kiến nghị, xử lý thêm/xóa công việc và file đính kèm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kiến nghị"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{id}")
    public ResponseEntity<RecommendationDto> updateRecommendation(
            @Parameter(description = "ID của kiến nghị cần cập nhật") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin kiến nghị",
                    content = @Content(
                            encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
            @RequestPart(value = "dto") RecommendationDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        RecommendationDto message = recommendService.updateRecommendation(id, dto, files);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @Operation(summary = "Xóa kiến nghị", description = "Xóa mềm kiến nghị bằng cách đánh dấu trường is_deleted")
    @DeleteMapping()
    public ResponseEntity<?> deleteRecommendation(@Parameter(description = "ID của các kiến nghị cần xóa") @RequestBody List<Long> ids) {
        recommendService.deleteRecommendations(ids);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Lấy chi tiết kiến nghị", description = "Lấy thông tin chi tiết của một kiến nghị theo ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<RecommendationDto> getRecommendation(@Parameter(description = "ID của kiến nghị") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.getRecommendationById(id));
    }

    @Operation(summary = "Lấy danh sách phản hồi kiến nghị", description = "Lấy thông tin các phản hồi của một kiến nghị theo ID")
    @GetMapping(path = "/response/{id}")
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendationResponse(@Parameter(description = "ID của kiến nghị") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.getRecommendationResponses(id));
    }

    @Operation(summary = "Đóng kiến nghị", description = "Đóng kiến nghi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đóng kiến nghị thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kiến nghị"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping(path = "/close/{id}")
    public ResponseEntity<?> closeRecommendation(@Parameter(description = "ID của kiến nghị") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.closeRecommendation(id));
    }

    @Operation(summary = "Thêm phản hồi kiến nghị", description = "Thêm phản hồi kiến nghi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm mới thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kiến nghị"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/response/{id}")
    public ResponseEntity<?> addRecommendationResponse(@Parameter(description = "ID của kiến nghị cần cập nhật") @PathVariable Long id,
                                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                               description = "Thông tin phản hồi kiến nghị",
                                                               content = @Content(
                                                                       encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE)
                                                               )
                                                       )
                                                       @RequestParam("responseDto") RecommendationResponseDto responseDto,
                                                       @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.addRecommendationResponse(id, responseDto, files));
    }

    @Operation(summary = "Từ chối kiến nghị", description = "Từ chối kiến nghi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Từ chối kiến nghị thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kiến nghị"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping(path = "/reject/{id}")
    public ResponseEntity<?> rejectRecommendation(@Parameter(description = "ID của kiến nghị") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.rejectRecommendation(id));
    }

    @Operation(summary = "Tiếp nhận kiến nghị", description = "Tiếp nhận kiến nghi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tiếp nhận kiến nghị thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không đúng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kiến nghị"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PutMapping(path = "/accept/{id}")
    public ResponseEntity<?> acceptRecommendation(@Parameter(description = "ID của kiến nghị") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.acceptRecommendation(id));
    }
}
