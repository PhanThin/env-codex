package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum OutstandingStatusEnum {
    NEW("Chưa xử lý"), IN_PROGRESS("Đang xử lý"), DONE("Hoàn thành"), CLOSED("Đã đóng");
    private final String vietnameseName;

    OutstandingStatusEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
}
