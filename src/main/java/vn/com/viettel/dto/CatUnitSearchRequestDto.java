package vn.com.viettel.dto;

import java.time.LocalDateTime;

public class CatUnitSearchRequestDto {

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;

    private String unitName;
    private String unitType;

    /** Default 'Y' if null. */
    private String isActive;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public LocalDateTime getCreatedAtFrom() {
        return createdAtFrom;
    }

    public void setCreatedAtFrom(LocalDateTime createdAtFrom) {
        this.createdAtFrom = createdAtFrom;
    }

    public LocalDateTime getCreatedAtTo() {
        return createdAtTo;
    }

    public void setCreatedAtTo(LocalDateTime createdAtTo) {
        this.createdAtTo = createdAtTo;
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

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}
