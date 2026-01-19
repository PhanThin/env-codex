package vn.com.viettel.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatManufacturerDto;
import vn.com.viettel.dto.CatManufacturerSearchRequestDto;
import vn.com.viettel.services.CatManufacturerService;
import vn.com.viettel.utils.Translator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/v1/cat-manufacturer")
@RequiredArgsConstructor

public class CatManufacturerController {
    private final CatManufacturerService catManufacturerService;


    @Autowired
    private CatManufacturerService service;
    @Autowired
    private Translator translator;

    private String msg(String key, Object... params) {
        return translator.getMessage(key, params);
    }

//    @PostMapping
//    public Map<String, Object> create(@RequestBody CatManufacturerDto dto) {
//        CatManufacturerDto data = service.create(dto);
//        Map<String, Object> res = new HashMap<>();
//        res.put("code", "SUCCESS");
//        res.put("message", msg("catManufacturer.create.success"));
//        res.put("data", data);
//        return res;
//    }
//
//    @PutMapping("/{id}")
//    public Map<String, Object> update(@PathVariable("id") Long id, @RequestBody CatManufacturerDto dto) {
//        CatManufacturerDto data = service.update(id, dto);
//        Map<String, Object> res = new HashMap<>();
//        res.put("code", "SUCCESS");
//        res.put("message", msg("catManufacturer.update.success"));
//        res.put("data", data);
//        return res;
//    }
//
//    @DeleteMapping
//    public Map<String, Object> delete(@RequestBody List<Long> manufacturerIds) {
//        service.delete(manufacturerIds);
//        Map<String, Object> res = new HashMap<>();
//        res.put("code", "SUCCESS");
//        res.put("message", msg("catManufacturer.delete.success"));
//        return res;
//    }
//
//    @GetMapping("/{id}")
//    public Map<String, Object> getDetail(@PathVariable("id") Long id) {
//        CatManufacturerDto data = service.getDetail(id);
//        Map<String, Object> res = new HashMap<>();
//        res.put("code", "SUCCESS");
//        res.put("message", msg("catManufacturer.get.success"));
//        res.put("data", data);
//        return res;
//    }
//
//    @GetMapping("/search")
//    public Map<String, Object> search(
//            @RequestParam(value = "createdAtFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtFrom,
//            @RequestParam(value = "createdAtTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtTo,
//            @RequestParam(value = "manufacturerCode", required = false) String manufacturerCode,
//            @RequestParam(value = "manufacturerName", required = false) String manufacturerName,
//            @RequestParam(value = "country", required = false) String country,
//            @RequestParam(value = "isActive", required = false) Boolean isActive,
//            @RequestParam(value = "page", required = false) Integer page,
//            @RequestParam(value = "size", required = false) Integer size,
//            @RequestParam(value = "sortBy", required = false) String sortBy,
//            @RequestParam(value = "sortDirection", required = false) String sortDirection
//    ) {
//        CatManufacturerSearchRequestDto request = CatManufacturerSearchRequestDto.builder()
//                .createdAtFrom(createdAtFrom)
//                .createdAtTo(createdAtTo)
//                .manufacturerCode(manufacturerCode)
//                .manufacturerName(manufacturerName)
//                .country(country)
//                .isActive(isActive)
//                .page(page)
//                .size(size)
//                .sortBy(sortBy)
//                .sortDirection(sortDirection)
//                .build();
//
//        Page<CatManufacturerDto> data = service.search(request);
//        Map<String, Object> res = new HashMap<>();
//        res.put("code", "SUCCESS");
//        res.put("message", msg("catManufacturer.search.success"));
//        res.put("data", data);
//        return res;
//    }
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        List<CatManufacturerDto> data = catManufacturerService.getAll(sortBy, sortDir);
        return ResponseEntity.ok(data);
    }

}
