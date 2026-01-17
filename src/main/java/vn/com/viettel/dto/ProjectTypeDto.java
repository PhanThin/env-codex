package vn.com.viettel.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO dùng chung cho create / update / response.
 * Lưu ý: client KHÔNG được gửi các trường audit (createdBy/createdAt/updatedBy/updatedAt).
 * Service sẽ tự set; nếu client có gửi lên cũng sẽ bị ignore.
 */
public class ProjectTypeDto implements Serializable {

    private Long id;
    private String projectTypeName;

    private Long updatedBy;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private LocalDateTime createdAt;

    private String isActive;  // 'Y'/'N'
    private String isDeleted; // 'Y'/'N'

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectTypeName() {
        return projectTypeName;
    }

    public void setProjectTypeName(String projectTypeName) {
        this.projectTypeName = projectTypeName;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
