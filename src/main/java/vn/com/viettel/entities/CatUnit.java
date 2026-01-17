package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "CAT_UNIT")
public class CatUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CAT_UNIT_SEQ")
    @SequenceGenerator(name = "CAT_UNIT_SEQ", sequenceName = "CAT_UNIT_SEQ", allocationSize = 1)
    @Column(name = "UNIT_ID", nullable = false)
    private Long id;

    @Column(name = "UNIT_NAME")
    private String unitName;

    @Column(name = "UNIT_TYPE")
    private String unitType;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "IS_ACTIVE", nullable = false)
    private String isActive;

    @Column(name = "IS_DELETED")
    private String isDeleted;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
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
}
