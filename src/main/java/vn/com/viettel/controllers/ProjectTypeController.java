package vn.com.viettel.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.ProjectTypeDto;
import vn.com.viettel.dto.ProjectTypeSearchRequestDto;
import vn.com.viettel.services.ProjectTypeService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/project-type")
public class ProjectTypeController {

    @Autowired
    private ProjectTypeService projectTypeService;

    @Autowired
    private Translator translator;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ProjectTypeDto dto) throws CustomException {
        ProjectTypeDto data = projectTypeService.create(dto);
        return ResponseEntity.ok(success("projectType.create.success", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable("id") Long id, @RequestBody ProjectTypeDto dto) throws CustomException {
        ProjectTypeDto data = projectTypeService.update(id, dto);
        return ResponseEntity.ok(success("projectType.update.success", data));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> delete(@RequestBody List<Long> projectTypeIds) throws CustomException {
        projectTypeService.delete(projectTypeIds);
        return ResponseEntity.ok(success("projectType.delete.success", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable("id") Long id) throws CustomException {
        ProjectTypeDto data = projectTypeService.getDetail(id);
        return ResponseEntity.ok(success("projectType.get.success", data));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@ModelAttribute ProjectTypeSearchRequestDto request) throws CustomException {
        Page<ProjectTypeDto> data = projectTypeService.search(request);
        return ResponseEntity.ok(success("projectType.search.success", data));
    }

    private Map<String, Object> success(String messageKey, Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("httpCode", 200);
        res.put("code", "SUCCESS");
        res.put("message", translator.getMessage(messageKey));
        res.put("data", data);
        return res;
    }
}
