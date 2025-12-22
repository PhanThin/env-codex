package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.viettel.dto.SysCategoryActionDTO;
import vn.com.viettel.services.SysCategoryActionService;
import vn.com.viettel.utils.excel.SysCategoryActionExcel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Hidden
@RestController
@RequestMapping("/api/v1/category-actions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class SysCategoryActionController {
  final SysCategoryActionService sysCategoryActionService;

  @PostMapping("/export")
  public void exportCategoryActions(@RequestBody SysCategoryActionDTO dto,
                                    HttpServletResponse response) throws IOException {
    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    String currentDateTime = dateFormatter.format(new Date());

    response.setHeader("Expires", "0");
    response.setHeader("Content-disposition", "attachment;filename=ListTopic_" + currentDateTime + ".xlsx");
    response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
    response.setHeader("Pragma", "public");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    List<SysCategoryActionDTO> sysCategoryActionDTOList = sysCategoryActionService.getSysCategoryActionForExport(dto);

    SysCategoryActionExcel.exportSysCategoryAction(sysCategoryActionDTOList, response);
  }
}
