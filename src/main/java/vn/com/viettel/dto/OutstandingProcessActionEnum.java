package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum OutstandingProcessActionEnum {
    SAVE_RESULT("Lưu xử lý"),
    SEND_FOR_ACCEPTANCE("Hoàn thành xử lý");
    private final String vietnameseName;

    OutstandingProcessActionEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
}
