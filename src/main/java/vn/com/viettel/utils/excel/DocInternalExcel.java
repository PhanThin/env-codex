package vn.com.viettel.utils.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;
import vn.com.viettel.dto.DocInternalDTO;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.entities.SysCategory;
import vn.com.viettel.utils.exceptions.CustomException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class DocInternalExcel extends ExcelHelpers {
    private static final String[] header = {"STT", "Mã văn bản", "Tên văn bản", "Loại văn bản", "Đơn vị sở hữu", "Topic",
            "Ngày ban hành", "Ngày hiệu lực", "Tình trạng hiệu lực", "Ngày hết hiệu lực"};

    public static void exportDocInternals(List<DocInternalDTO> docInternals, HttpServletResponse response) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("VBNB");
        int rowIndex = 0;
        writeHeader(sheet, rowIndex, header);
        rowIndex++;
        for (var item : docInternals) {
            Row row = sheet.createRow(rowIndex);
            int columnCount = 0;
            writeCell(sheet, row, columnCount++, rowIndex);
            writeCell(sheet, row, columnCount++, item.getCode());
            writeCell(sheet, row, columnCount++, item.getTitle());
            writeCell(sheet, row, columnCount++, item.getTypeName());
            writeCell(sheet, row, columnCount++, item.getOrgOwnName());
            writeCell(sheet, row, columnCount++, item.getTopicName());
            writeCell(sheet, row, columnCount++, item.getIssuedDate() != null ? dateFormat.format(item.getIssuedDate()) : "");
            writeCell(sheet, row, columnCount++, item.getEffectiveDate() != null ? dateFormat.format(item.getEffectiveDate()) : "");
            writeCell(sheet, row, columnCount++, (Objects.equals(item.getEffectiveStatus(), "1") ? "Còn hiệu lực" : "Hết hiệu lực"));
            writeCell(sheet, row, columnCount, item.getExpireDate() != null ? dateFormat.format(item.getExpireDate()) : "");
            rowIndex++;
        }

        autoSizeColumns(sheet);

        exportStream(workbook, response);
    }

    public static void templateImportDocInternals(List<SysCategoryDTO> sysCategories, HttpServletResponse response) {
        String fileTemplate = "template_thu_vien_VBNB.xlsx";
        File file = new File(fileTemplate);
        if (!file.exists()) throw new CustomException("File không tồn tại!");

        if (!CollectionUtils.isEmpty(sysCategories)) {
            try (Workbook workbook = new XSSFWorkbook(new FileInputStream(fileTemplate))) {
                List<SysCategoryDTO> listLoaiVBNB = sysCategories.stream().filter(o -> o.getType() == SysCategory.Type.LOAI_VB.value).collect(Collectors.toList());
                writeSysCategoriesToTemplate(workbook, 1, listLoaiVBNB);

                List<SysCategoryDTO> listDVSoHuu = sysCategories.stream().filter(o -> o.getType() == SysCategory.Type.DV_SO_HUU.value).collect(Collectors.toList());
                writeSysCategoriesToTemplate(workbook, 2, listDVSoHuu);

                List<SysCategoryDTO> listTopic = sysCategories.stream().filter(o -> o.getType() == SysCategory.Type.TOPIC.value).collect(Collectors.toList());
                writeSysCategoriesToTemplate(workbook, 3, listTopic);

                Name namedCell1 = workbook.createName();
                namedCell1.setNameName("LOAI_VB_NAME");
                String reference1 = "'Loại VB'!$B$2:$B$" + listLoaiVBNB.size();
                namedCell1.setRefersToFormula(reference1);

                Name namedCell3 = workbook.createName();
                namedCell3.setNameName("DV_SO_HUU_NAME");
                String reference3 = "'Đơn vị sở hữu'!$B$2:$B$" + listDVSoHuu.size();
                namedCell3.setRefersToFormula(reference3);

                Name namedCell5 = workbook.createName();
                namedCell5.setNameName("TOPIC_NAME");
                String reference5 = "'Topic'!$B$2:$B$" + listTopic.size();
                namedCell5.setRefersToFormula(reference5);

                Sheet sheet = workbook.getSheetAt(0);
                DataValidationHelper dvHelper1 = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint1 = dvHelper1.createFormulaListConstraint("LOAI_VB_NAME");
                CellRangeAddressList addressList1 = new CellRangeAddressList(3, 1003, 2, 2); // C3 - C1003
                DataValidation validation1 = dvHelper1.createValidation(dvConstraint1, addressList1);
                validation1.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation1.createErrorBox("Invalid Option", "Please select a valid option from the list.");
                sheet.addValidationData(validation1);

                DataValidationHelper dvHelper2 = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint2 = dvHelper2.createFormulaListConstraint("DV_SO_HUU_NAME");
                CellRangeAddressList addressList2 = new CellRangeAddressList(3, 1003, 4, 4); // E3 - E1003
                DataValidation validation2 = dvHelper1.createValidation(dvConstraint2, addressList2);
                validation2.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation2.createErrorBox("Invalid Option", "Please select a valid option from the list.");
                sheet.addValidationData(validation2);

                DataValidationHelper dvHelper3 = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint3 = dvHelper3.createFormulaListConstraint("TOPIC_NAME");
                CellRangeAddressList addressList3 = new CellRangeAddressList(3, 1003, 6, 6); // E3 - E1003
                DataValidation validation3 = dvHelper1.createValidation(dvConstraint3, addressList3);
                validation3.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation3.createErrorBox("Invalid Option", "Please select a valid option from the list.");
                sheet.addValidationData(validation3);

                workbook.setForceFormulaRecalculation(true);
                // Create a formula cell for VLOOKUP in a target cell (e.g., C1)
                for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                    Row formulaRow = sheet.getRow(i);

                    // Set the VLOOKUP formula in the formula cell
                    Cell formulaCell1 = formulaRow.getCell(3); // Cell D4
                    formulaCell1.setCellFormula("IFERROR(VLOOKUP(C" + (i + 1) + ",'Loại VB'!B2:C" + listLoaiVBNB.size() + ",2,FALSE),\"\")");

                    Cell formulaCell2 = formulaRow.getCell(5); // Cell F4
                    formulaCell2.setCellFormula("IFERROR(VLOOKUP(E" + (i + 1) + ",'Đơn vị sở hữu'!B2:C" + listDVSoHuu.size() + ",2,FALSE),\"\")");

                    Cell formulaCell3 = formulaRow.getCell(7); // Cell h4
                    formulaCell3.setCellFormula("IFERROR(VLOOKUP(G" + (i + 1) + ",'Topic'!B2:C" + listTopic.size() + ",2,FALSE),\"\")");
                }
                exportStream(workbook, response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void writeSysCategoriesToTemplate(Workbook workbook, int index, List<SysCategoryDTO> data) {
        if (!CollectionUtils.isEmpty(data)) {
            Sheet sheet = workbook.getSheetAt(index);
            int rowIndex = 1;
            for (var item : data) {
                Row row = sheet.createRow(rowIndex);
                int columnCount = 0;
                writeCell(sheet, row, columnCount++, rowIndex);
                writeCell(sheet, row, columnCount++, item.getName());
                writeCell(sheet, row, columnCount, item.getCode());
                rowIndex++;
            }
            autoSizeColumns(sheet);
        }
    }

    public static void autoSizeColumns(Sheet sheet) {
        if (sheet.getPhysicalNumberOfRows() > 0) {
            Row row = sheet.getRow(sheet.getFirstRowNum());
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                sheet.autoSizeColumn(columnIndex);
            }
        }
    }
}
