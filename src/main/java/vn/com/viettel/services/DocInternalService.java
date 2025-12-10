package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.DocInternalDTO;
import vn.com.viettel.dto.UploadMessageDTO;
import vn.com.viettel.entities.DocInternal;

import java.io.IOException;
import java.util.List;

public interface DocInternalService {

    List<DocInternalDTO> getDocInternalsForExport(DocInternalDTO dto);

    DocInternal createDocInternal(DocInternalDTO dto, MultipartFile[] files);

    DocInternal updateDocInternal(DocInternalDTO dto, MultipartFile[] files);

    UploadMessageDTO importDocInternals(MultipartFile file) throws IOException;

    UploadMessageDTO validateImportDocInternals(MultipartFile file) throws IOException;

    Page<DocInternalDTO> getDocInternalsForBuilding(DocInternalDTO dto);
}
