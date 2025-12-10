package vn.com.viettel.minio.services;

import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.minio.dto.ObjectFileDTO;

import java.util.List;

public interface FileService {
    List<ObjectFileDTO> uploadFiles(String tenant, String channel, MultipartFile[] files);

    byte[] getFile(String tenant, String filePath);
}
