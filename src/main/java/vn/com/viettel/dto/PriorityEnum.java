package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum PriorityEnum {
    HIGH_PRIORITY("Rất quan trọng"), PRIORITY("Quan trọng"), LOW_PRIORITY("Bình thường");

    private final String vietnameseName;

    PriorityEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
}
