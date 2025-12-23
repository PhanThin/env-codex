package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum RecommendationStatusEnum {
    NEW("Chưa xử lý"), DONE("Đã xử lý");

    private final String vietnameseName;

    RecommendationStatusEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
}
