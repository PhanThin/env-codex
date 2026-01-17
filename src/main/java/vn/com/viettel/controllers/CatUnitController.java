package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatUnitDto;
import vn.com.viettel.dto.CatUnitSearchRequestDto;
import vn.com.viettel.services.CatUnitService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "16. Quản lý danh mục đơn vị", description = "API quản lý danh mục đơn vị")
@RestController
@RequestMapping("/api/v1/cat-unit")
public class CatUnitController {

    @Autowired
    private CatUnitService catUnitService;

    @Autowired
    private Translator translator;
//
//    @PostMapping
//    public ResponseEntity<CatUnitDto> create(@RequestBody CatUnitDto dto) throws CustomException {
//        CatUnitDto result = catUnitService.create(dto);
//        return ResponseEntity.ok(result);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<CatUnitDto> update(@PathVariable("id") Long id, @RequestBody CatUnitDto dto) throws CustomException {
//        CatUnitDto result = catUnitService.update(id, dto);
//        return ResponseEntity.ok(result);
//    }
//
//    @DeleteMapping
//    public ResponseEntity<?> delete(@RequestBody List<Long> unitIds) throws CustomException {
//        catUnitService.delete(unitIds);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<CatUnitDto> getDetail(@PathVariable("id") Long id) throws CustomException {
//        CatUnitDto result = catUnitService.getDetail(id);
//        return ResponseEntity.ok(result);
//    }
//
//    @PostMapping("/search")
//    public ResponseEntity<Page<CatUnitDto>> search(CatUnitSearchRequestDto request) throws CustomException {
//        Page<CatUnitDto> result = catUnitService.search(request);
//        return ResponseEntity.ok(result);
//    }

    @GetMapping
    @Operation(summary = "Lấy danh sách đơn vị theo loại (MASS: khổi lượng, LENGTH: chiều dài, EQUIPMENT: thiết bị)")
    public ResponseEntity<List<CatUnitDto>> findAllByType(@RequestParam("type") String type) throws CustomException {
        return ResponseEntity.ok(catUnitService.findAllUnitType(type));
    }

    private Map<String, Object> buildSuccess(String messageKey, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "SUCCESS");
        body.put("message", translator.getMessage(messageKey));
        body.put("data", data);
        return body;
    }
}
