package vn.com.viettel.dto;

public enum OutstandingAcceptianceTypeEnum {

    WORK_ACCEPTANCE("Nghiệm thu công việc"),
    ITEM_COMPLETION_ACCEPTANCE("Nghiệm thu hoàn thành hạng mục"),
    SURVEY_COMPLETION_ACCEPTANCE("Nghiệm thu hoàn thành khảo sát");

    private final String vietnameseName;

    OutstandingAcceptianceTypeEnum(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

}
