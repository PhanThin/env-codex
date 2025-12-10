package vn.com.viettel.utils.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.com.viettel.entities.SysCategory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SysCategoryExcel extends ExcelHelpers {
    private static final String[] TopicHeader = {"STT", "Mã Topic", "Topic", "Trạng thái"};
    private static final String[] THKSHeader = {"STT", "Mã cấp", "Tên cấp", "Trạng thái"};
    private static final String[] DVSHHeader = {"STT", "Mã đơn vị sở hữu", "Đơn vị sở hữu", "Trạng thái"};
    private static final String[] LoaiVBNBHeader = {"STT", "Mã loại VBNB", "Loại VBNB", "Trạng thái"};

    public static void exportSysCategory(List<SysCategory> sysCategories, HttpServletResponse response, Short type) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        String name = "";
        switch (type) {
            case 1: name = "Topic"; break;
            case 2: name = "Cấp"; break;
            case 3: name = "Đơn vị sở hữu"; break;
            case 4: name = "Loại văn bản nội bộ"; break;
            default: break;
        }
        Sheet sheet = workbook.createSheet(name);
        int rowIndex = 0;
        switch (type) {
            case 1: writeHeader(sheet, rowIndex, TopicHeader); break;
            case 2: writeHeader(sheet, rowIndex, THKSHeader); break;
            case 3: writeHeader(sheet, rowIndex, DVSHHeader); break;
            case 4: writeHeader(sheet, rowIndex, LoaiVBNBHeader); break;
            default: break;
        }
        rowIndex++;
        for (var item : sysCategories) {
            Row row = sheet.createRow(rowIndex);
            int columnCount = 0;
            writeCell(sheet, row, columnCount++, rowIndex);
            writeCell(sheet, row, columnCount++, item.getCode());
            writeCell(sheet, row, columnCount++, item.getName());
            writeCell(sheet, row, columnCount, (Objects.equals(item.getStatus(), "1") ? "Hoạt động" : "Không hoạt động"));
            rowIndex++;
        }

        autoSizeColumns(sheet);

        exportStream(workbook, response);
    }
}
