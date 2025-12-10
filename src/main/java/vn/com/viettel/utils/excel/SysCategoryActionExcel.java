package vn.com.viettel.utils.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.com.viettel.dto.SysCategoryActionDTO;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SysCategoryActionExcel extends ExcelHelpers {
    private static final String[] HoatDongHeader = {"STT", "Mã hoạt động cấp 1", "Hoạt động cấp 1", "Mã hoạt động cấp 2", "Hoạt động cấp 2", "Trạng thái"};

    public static void exportSysCategoryAction(List<SysCategoryActionDTO> sysCategoryActionDTOList, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Hoạt động");
        int rowIndex = 0;
        writeHeader(sheet, rowIndex, HoatDongHeader);
        rowIndex++;
        for (var item : sysCategoryActionDTOList) {
            Row row = sheet.createRow(rowIndex);
            int columnCount = 0;
            writeCell(sheet, row, columnCount++, rowIndex);
            writeCell(sheet, row, columnCount++, item.getCodeLevel1());
            writeCell(sheet, row, columnCount++, item.getNameLevel1());
            writeCell(sheet, row, columnCount++, item.getCodeLevel2());
            writeCell(sheet, row, columnCount++, item.getNameLevel2());
            writeCell(sheet, row, columnCount, (Objects.equals(item.getStatus(), "1") ? "Hoạt động" : "Không hoạt động"));
            rowIndex++;
        }

        autoSizeColumns(sheet);

        exportStream(workbook, response);
    }
}
