package vn.com.viettel.dto;

import lombok.Getter;

@Getter
public enum OutstandingAcceptanceTypeEnum {

    WORK_ACCEPTANCE("Nghiệm thu công việc"),
    ITEM_COMPLETION_ACCEPTANCE("Nghiệm thu hoàn thành hạng mục"),
    SURVEY_COMPLETION_ACCEPTANCE("Nghiệm thu hoàn thành khảo sát");

    private final String vietnameseName;

    OutstandingAcceptanceTypeEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

}
