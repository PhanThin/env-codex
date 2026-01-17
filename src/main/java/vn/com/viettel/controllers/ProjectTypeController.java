package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "15. Quản lý loại dự án", description = "API quản lý loại dự án")
@RestController
@RequestMapping("/api/v1/project-type")
public class ProjectTypeController {

    @Autowired
    private ProjectTypeService projectTypeService;

    @Autowired
    private Translator translator;

//    @PostMapping
//    public ResponseEntity<ProjectTypeDto> create(@RequestBody ProjectTypeDto dto) throws CustomException {
//        ProjectTypeDto data = projectTypeService.create(dto);
//        return ResponseEntity.ok(data);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ProjectTypeDto> update(@PathVariable("id") Long id, @RequestBody ProjectTypeDto dto) throws CustomException {
//        ProjectTypeDto data = projectTypeService.update(id, dto);
//        return ResponseEntity.ok(data);
//    }
//
//    @DeleteMapping
//    public ResponseEntity<?> delete(@RequestBody List<Long> projectTypeIds) throws CustomException {
//        projectTypeService.delete(projectTypeIds);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ProjectTypeDto> getDetail(@PathVariable("id") Long id) throws CustomException {
//        ProjectTypeDto data = projectTypeService.getDetail(id);
//        return ResponseEntity.ok(data);
//    }
//
//    @PostMapping("/search")
//    public ResponseEntity<Page<ProjectTypeDto>> search(@ModelAttribute ProjectTypeSearchRequestDto request) throws CustomException {
//        Page<ProjectTypeDto> data = projectTypeService.search(request);
//        return ResponseEntity.ok(data);
//    }

    @GetMapping
    public ResponseEntity<List<ProjectTypeDto>> findAll() throws CustomException {
        return ResponseEntity.ok(projectTypeService.getAllProjectType());
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
