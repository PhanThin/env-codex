package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum OutstandingAcceptanceResultEnum {
    ACCEPTED("Hoàn thành"),
    REJECTED("Từ chối");
    private final String vietnameseName;

    OutstandingAcceptanceResultEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }
}
