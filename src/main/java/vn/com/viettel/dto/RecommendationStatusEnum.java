package vn.com.viettel.dto;

import lombok.Getter;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.Optional;

@Getter
public enum RecommendationStatusEnum {
    NEW("CHUA_XU_LY"), IN_PROGRESS("DANG_XU_LY"), DONE("HOAN_THANH");

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

}
