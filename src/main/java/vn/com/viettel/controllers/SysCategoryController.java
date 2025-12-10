package vn.com.viettel.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.dto.UploadMessageDTO;
import vn.com.viettel.entities.SysCategory;
import vn.com.viettel.services.SysCategoryService;
import vn.com.viettel.utils.excel.ExcelHelpers;
import vn.com.viettel.utils.excel.SysCategoryExcel;
import vn.com.viettel.utils.exceptions.CustomException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class SysCategoryController {
    final SysCategoryService sysCategoryService;

    @PostMapping("/export")
    public void exportCategories(@RequestBody SysCategoryDTO dto,
                                 HttpServletResponse response) throws IOException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Expires", "0");
        response.setHeader("Content-disposition", "attachment;filename=ListTopic_" + currentDateTime + ".xlsx");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        List<SysCategory> sysCategories = sysCategoryService.getSysCategoriesForExport(dto);

        SysCategoryExcel.exportSysCategory(sysCategories, response, dto.getType());
    }

    @PostMapping("/import/validate")
    public ResponseEntity<Object> validateImportFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(defaultValue = "1") String type) throws IOException {
        if (!ExcelHelpers.hasExcelFormat(file)) {
          throw new CustomException("msg.common.validate.import.file");
        }
        UploadMessageDTO message = sysCategoryService.validateImportSysCategories(file, type);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/import")
    public ResponseEntity<Object> importCategories(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(defaultValue = "1") String type) throws IOException {
        if (!ExcelHelpers.hasExcelFormat(file)) {
            throw new CustomException("msg.common.validate.import.file");
        }
        UploadMessageDTO message = sysCategoryService.importSysCategories(file, type);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @GetMapping("/import/template")
    public ResponseEntity<Object> downloadTemplateImportCategories(@RequestParam(defaultValue = "1") String type) {
        String fileTemplate = "config_topic_template.xlsx";
        switch (type) {
            case "1": fileTemplate = "config_topic_template.xlsx"; break;
            case "2": fileTemplate = "config_cap_thuc_hien_va_kiem_soat_template.xlsx"; break;
            case "3": fileTemplate = "config_don_vi_so_huu_template.xlsx"; break;
            case "4": fileTemplate = "config_loai_van_ban_noi_bo_template.xlsx"; break;
            default: break;
        }
        File file = new File("" + File.separator + fileTemplate);
        if (!file.exists()) {
            throw new CustomException("msg.common.validate.import.file.template.not-found");
        }

        UrlResource resource;
        try {
            resource = new UrlResource(file.toURI());
        } catch (MalformedURLException ex) {
            log.error("/api/v1/category/import/template: ", ex);
            throw new CustomException("msg.common.validate.import.file.template.not-found");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(resource);
    }
}
