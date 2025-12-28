package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum RecommendationStatusEnum {
    NEW("Chưa xử lý"), IN_PROGRESS("Đang xử lý"), DONE("Đã xử lý"), CLOSED("Đã đóng");
    private final String vietnameseName;

    RecommendationStatusEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
}
