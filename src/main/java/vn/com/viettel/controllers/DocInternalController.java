package vn.com.viettel.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.DocInternalDTO;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.dto.UploadMessageDTO;
import vn.com.viettel.repositories.jpa.SysCategoryRepositoryJPA;
import vn.com.viettel.services.DocInternalService;
import vn.com.viettel.utils.excel.DocInternalExcel;
import vn.com.viettel.utils.excel.ExcelHelpers;
import vn.com.viettel.utils.exceptions.CustomException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doc-internal")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class DocInternalController {
    final DocInternalService docInternalService;
    final SysCategoryRepositoryJPA sysCategoryRepositoryJPA;

    @PostMapping("/export")
    public void getDocInternalsForExport(@RequestBody DocInternalDTO dto, HttpServletResponse response) throws IOException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Expires", "0");
        response.setHeader("Content-disposition", "attachment;filename=ListThuVienVBNB_" + currentDateTime + ".xlsx");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        List<DocInternalDTO> docInternals = docInternalService.getDocInternalsForExport(dto);

        DocInternalExcel.exportDocInternals(docInternals, response);

    }

    @PostMapping("/import/validate")
    public ResponseEntity<Object> validateImportDocInternals(@RequestParam("file") MultipartFile file) throws IOException {
        if (!ExcelHelpers.hasExcelFormat(file)) throw new CustomException("Định dạng cho phép .xlsx");
        UploadMessageDTO message = docInternalService.validateImportDocInternals(file);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/import")
    public ResponseEntity<Object> importDocInternals(@RequestParam("file") MultipartFile file) throws IOException {
      if (!ExcelHelpers.hasExcelFormat(file)) throw new CustomException("Định dạng cho phép .xlsx");
      UploadMessageDTO message = docInternalService.importDocInternals(file);
      return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/import/template")
    public void downloadTemplateImportDocInternal(HttpServletResponse response) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Expires", "0");
        response.setHeader("Content-disposition", "attachment;filename=TemplateThuVienVBNB_" + currentDateTime + ".xlsx");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        List<SysCategoryDTO> sysCategories = sysCategoryRepositoryJPA.getAllSysCategories();

        DocInternalExcel.templateImportDocInternals(sysCategories, response);
    }
}
