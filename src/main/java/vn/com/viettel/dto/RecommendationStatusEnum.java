package vn.com.viettel.dto;

import vn.com.viettel.utils.exceptions.CustomException;

import java.util.Optional;

public enum RecommendationStatusEnum {
    UN_PROCESS("CHUA_XU_LY"), PROCESSING("DANG_XU_LY"), FINISHED("HOAN_THANH");

    private final String value;

    RecommendationStatusEnum(String value) {
        this.value = value;
    }

    public static Optional<RecommendationStatusEnum> fromString(String text) throws CustomException {
        if (text != null) {
            for (RecommendationStatusEnum statusEnum : RecommendationStatusEnum.values()) {
                if (text.equalsIgnoreCase(statusEnum.getValue())) {
                    return Optional.of(statusEnum);
                }
            }
        }
        return Optional.empty();
    }

    public String getValue() {
        return value;
    }
}
