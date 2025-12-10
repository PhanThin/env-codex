package vn.com.viettel.services;

import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.dto.UploadMessageDTO;
import vn.com.viettel.entities.SysCategory;

import java.io.IOException;
import java.util.List;

public interface SysCategoryService {

    List<SysCategory> getSysCategoriesForExport(SysCategoryDTO dto);

    UploadMessageDTO importSysCategories(MultipartFile file, String type) throws IOException;

    UploadMessageDTO validateImportSysCategories(MultipartFile file, String type) throws IOException;
}
