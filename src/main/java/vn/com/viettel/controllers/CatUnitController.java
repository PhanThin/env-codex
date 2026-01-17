package vn.com.viettel.controllers;

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

@RestController
@RequestMapping("/api/v1/cat-unit")
public class CatUnitController {

    @Autowired
    private CatUnitService catUnitService;

    @Autowired
    private Translator translator;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CatUnitDto dto) throws CustomException {
        CatUnitDto result = catUnitService.create(dto);
        return ResponseEntity.ok(buildSuccess("catUnit.create.success", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable("id") Long id, @RequestBody CatUnitDto dto) throws CustomException {
        CatUnitDto result = catUnitService.update(id, dto);
        return ResponseEntity.ok(buildSuccess("catUnit.update.success", result));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> delete(@RequestBody List<Long> unitIds) throws CustomException {
        catUnitService.delete(unitIds);
        return ResponseEntity.ok(buildSuccess("catUnit.delete.success", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable("id") Long id) throws CustomException {
        CatUnitDto result = catUnitService.getDetail(id);
        return ResponseEntity.ok(buildSuccess("catUnit.get.success", result));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(CatUnitSearchRequestDto request) throws CustomException {
        Page<CatUnitDto> result = catUnitService.search(request);
        return ResponseEntity.ok(buildSuccess("catUnit.search.success", result));
    }

    private Map<String, Object> buildSuccess(String messageKey, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "SUCCESS");
        body.put("message", translator.getMessage(messageKey));
        body.put("data", data);
        return body;
    }
}
