package vn.com.viettel.services.impl;

import io.minio.RemoveObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.minio.dto.ObjectFileDTO;
import vn.com.viettel.minio.services.FileService;
import vn.com.viettel.services.StorageService;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final FileService libraryFileService;
    private final MinioClient minioClient;

    @Override
    public List<ObjectFileDTO> uploadFiles(String tenant, String channel, MultipartFile[] files) {
        return libraryFileService.uploadFiles(tenant, channel, files);
    }

    @Override
    public byte[] getFile(String tenant, String filePath) {
        return libraryFileService.getFile(tenant, filePath);
    }

    @Override
    public boolean deleteFile(String bucketName, String filePath) {
        try {
            log.info("Request delete MinIO file - Bucket: {}, Path: {}", bucketName, filePath);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            return true;
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage());
            return false;
        }
    }
}