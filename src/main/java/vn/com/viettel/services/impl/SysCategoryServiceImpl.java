package vn.com.viettel.services.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.dto.UploadMessageDTO;
import vn.com.viettel.entities.SysCategory;
import vn.com.viettel.repositories.jpa.SysCategoryRepositoryJPA;
import vn.com.viettel.services.SysCategoryService;
import vn.com.viettel.utils.excel.ExcelHelpers;
import vn.com.viettel.utils.exceptions.CustomException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SysCategoryServiceImpl implements SysCategoryService {
    final SysCategoryRepositoryJPA sysCategoryRepository;


    @Override
    public List<SysCategory> getSysCategoriesForExport(SysCategoryDTO dto) {
        return sysCategoryRepository.getSysCategoriesForExport(dto);
    }

    @Override
    public UploadMessageDTO importSysCategories(MultipartFile file, String type) throws IOException {
        byte[] byteArr = file.getBytes();
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        byte[] bytes;
        UploadMessageDTO message = new UploadMessageDTO();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(byteArr))) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validate match template
            boolean isMatchTemplate = true;
            String fileNameTemplate = "config_topic_template.xlsx";
            switch (type) {
                case "1": fileNameTemplate = "config_topic_template.xlsx";break;
                case "2": fileNameTemplate = "config_cap_thuc_hien_va_kiem_soat_template.xlsx";break;
                case "3": fileNameTemplate = "config_don_vi_so_huu_template.xlsx";break;
                case "4": fileNameTemplate = "config_loai_van_ban_noi_bo_template.xlsx";break;
                default: break;
            }
            File fileTemplate = new File("" + File.separator + fileNameTemplate);
            try (Workbook workbookTemplate = new XSSFWorkbook(new FileInputStream(fileTemplate))) {
                Sheet sheetTemplate = workbookTemplate.getSheetAt(0);
                if (!ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(0), sheet.getRow(0))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(1), sheet.getRow(1))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(2), sheet.getRow(2))) {
                    isMatchTemplate = false;
                }
            }

            if (!isMatchTemplate) throw new CustomException("msg.common.validate.import.file.template.invalid");

            int totalRows = sheet.getPhysicalNumberOfRows();
            log.info("importSysCategory Sheet row " + totalRows);
            if (totalRows > 1003) throw new CustomException("Tối đa tổng số bản ghi import là 1000");
            int totalInsert = 0;
            int totalUpdate = 0;
            int totalError = 0;
            String name = "";
            switch (type) {
                case "1": name = "Topic"; break;
                case "2": name = "Cấp"; break;
                case "3": name = "Đơn vị sở hữu"; break;
                case "4": name = "Loại văn bản nội bộ"; break;
                default: break;
            }
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                if (ExcelHelpers.hasEmptyAllCellOnRow(sheet.getRow(i), 0, 3)) {
                    Row row = sheet.getRow(i);
                    if (validateFileImport(row, type)) {
                        if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(3)), "1")) {
                            SysCategory sysCategory = new SysCategory();
                            sysCategory.setName(ExcelHelpers.getStringCellValue(row.getCell(1)));
                            sysCategory.setStatus(ExcelHelpers.getStringCellValue(row.getCell(2)));
                            switch(type) {
                                case "1":
                                    sysCategory.setType(SysCategory.Type.TOPIC.value);
                                    sysCategory.setCode("TP"); break;
                                case "2":
                                    sysCategory.setType(SysCategory.Type.CAP_THKS.value);
                                    sysCategory.setCode("THKS"); break;
                                case "3":
                                    sysCategory.setType(SysCategory.Type.DV_SO_HUU.value);
                                    sysCategory.setCode("DV"); break;
                                case "4":
                                    sysCategory.setType(SysCategory.Type.LOAI_VB.value);
                                    sysCategory.setCode("VB"); break;
                                default: break;
                            }
                            sysCategoryRepository.save(sysCategory);
                            totalInsert++;

                            writeResult(row, "Success", "Thành công");
                        } else {
                            String code = ExcelHelpers.getStringCellValue(row.getCell(0));
                            Optional<SysCategory> sysCategoryOpt = sysCategoryRepository.findByCodeAndType(code, Short.parseShort(type));
                            if (sysCategoryOpt.isEmpty()) {
                                writeResult(row, "Failed", "Không tồn tại mã " + name.toLowerCase() + " "  + code);
                                totalError++;
                            } else {
                                SysCategory sysCategory = sysCategoryOpt.get();
                                sysCategory.setName(ExcelHelpers.getStringCellValue(row.getCell(1)));
                                sysCategory.setStatus(ExcelHelpers.getStringCellValue(row.getCell(2)));
                                sysCategoryRepository.save(sysCategory);
                                totalUpdate++;
                                writeResult(row, "Success", "Thành công");
                            }
                        }
                    } else {
                        totalError++;
                    }
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            bytes = os.toByteArray();

            if (totalError > 0) {
                String dataExport = Base64.getEncoder().encodeToString(bytes);
                message.setMessageCode("001");
                message.setMessageDesc("Thêm mới: " + totalInsert + " | Cập nhật: " + totalUpdate + " | Lỗi: " + totalError);
                message.setFileData(dataExport);
            } else {
                message.setMessageCode("000");
                message.setMessageDesc("Thêm mới: " + totalInsert + " | Cập nhật: " + totalUpdate);
            }
        } catch (Exception e) {
            log.error("Import noi dung khong thanh cong ", e);
            if (e instanceof CustomException) throw new CustomException(e.getMessage());
            throw new CustomException("msg.common.import.file.failed");
        }
        return message;
    }

    @Override
    public UploadMessageDTO validateImportSysCategories(MultipartFile file, String type) throws IOException {
        byte[] byteArr = file.getBytes();
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        byte[] bytes;
        UploadMessageDTO message = new UploadMessageDTO();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(byteArr))) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validate match template
            boolean isMatchTemplate = true;
            String fileNameTemplate = "config_topic_template.xlsx";
            switch (type) {
                case "1": fileNameTemplate = "config_topic_template.xlsx";break;
                case "2": fileNameTemplate = "config_cap_thuc_hien_va_kiem_soat_template.xlsx";break;
                case "3": fileNameTemplate = "config_don_vi_so_huu_template.xlsx";break;
                case "4": fileNameTemplate = "config_loai_van_ban_noi_bo_template.xlsx";break;
                default: break;
            }
            File fileTemplate = new File("" + File.separator + fileNameTemplate);
            try (Workbook workbookTemplate = new XSSFWorkbook(new FileInputStream(fileTemplate))) {
                Sheet sheetTemplate = workbookTemplate.getSheetAt(0);
                if (!ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(0), sheet.getRow(0))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(1), sheet.getRow(1))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(2), sheet.getRow(2))) {
                    isMatchTemplate = false;
                }
            }

            if (!isMatchTemplate) throw new CustomException("msg.common.validate.import.file.template.invalid");

            int totalRows = sheet.getPhysicalNumberOfRows();
            log.info("validateImportSysCategories Sheet row " + totalRows);
            if (totalRows > 1003) throw new CustomException("Tối đa tổng số bản ghi import là 1000");
            int totalError = 0;
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                if (ExcelHelpers.hasEmptyAllCellOnRow(sheet.getRow(i), 0, 3)) {
                    Row row = sheet.getRow(i);
                    if (validateFileImport(row, type)) {
                        writeResult(row, "Success", "Thành công");
                    } else {
                        totalError++;
                    }
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            bytes = os.toByteArray();

            if (totalError > 0) {
                String dataExport = Base64.getEncoder().encodeToString(bytes);
                message.setMessageCode("001");
                message.setMessageDesc("Validate file thành không. Có " + totalError + " bản ghi lỗi");
                message.setFileData(dataExport);
            } else {
                message.setMessageCode("000");
                message.setMessageDesc("Validate file thành không. Không có bản ghi lỗi");
            }
        } catch (Exception e) {
            log.error("Validate Import noi dung khong thanh cong ", e);
            if (e instanceof CustomException) throw new CustomException(e.getMessage());
        }
        return message;
    }

    private boolean validateFileImport(Row row, String type) {
        String msg = "";
        String name = "";
        switch (type) {
            case "1": name = "Topic"; break;
            case "2": name = "Tên cấp"; break;
            case "3": name = "Đơn vị sở hữu"; break;
            case "4": name = "Loại văn bản nội bộ"; break;
            default: break;
        }

        if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(3)), "1")) {
            if (!Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(0)), "")) msg += "|Mã " + name.toLowerCase().replace("tên ", "") + " không được phép nhập vì hệ thống sẽ tự sinh";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(1)), "")) {
                msg += "|" + name + " không được để trống";
            } else {
                if (Objects.requireNonNull(ExcelHelpers.getStringCellValue(row.getCell(1))).length() > 1000) msg += "|" + name + " không được vượt quá 1000 ký tự";
            }
        } else if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(3)), "2")) {
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(0)), "")) {
                msg += "|Mã " + name.toLowerCase().replace("tên ", "") + " không được để trống";
            } else {
                if (Objects.requireNonNull(ExcelHelpers.getStringCellValue(row.getCell(0))).length() > 36) msg += "|Mã " + name.toLowerCase().replace("tên ", "") + " không được vượt quá 36 ký tự";
            }
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(1)), "")) {
                msg += "|" + name + " không được để trống";
            } else {
                if (Objects.requireNonNull(ExcelHelpers.getStringCellValue(row.getCell(1))).length() > 1000) msg += "|" + name + " không được vượt quá 1000 ký tự";
            }
        } else {
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(3)), "")) msg += "|Hành động không được để trống";
            else msg += "|Hành động không hợp lệ|Hành động chỉ chấp nhận 2 giá trị: 1: Thêm mới và 2: Cập nhật";
        }

        if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(2)), "")) msg += "|Trạng thái không được để trống";
        else if (!Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(2)), "1") && !Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(2)), "0")) {
            msg += "|Trạng thái không hợp lệ|Trạng thái chỉ chấp nhận 2 giá trị: 1: Hoạt động, 0: Không hoạt động";
        }

        if (!msg.isEmpty()) {
            writeResult(row, "Failed", msg);
            return false;
        }
        return true;
    }

    private void writeResult(Row row, String resultCode, String resultMgs) {
        Cell result = row.createCell(4);
        result.setCellType(CellType.STRING);
        result.setCellValue(resultCode);
        Cell description = row.createCell(5);
        description.setCellType(CellType.STRING);
        description.setCellValue(resultMgs);
    }
}
