package vn.com.viettel.utils.validates;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;


public class NumberFormatValidator implements ConstraintValidator<NumberFormat, String> {
    @Override
    public boolean isValid(String data, ConstraintValidatorContext constraintValidatorContext) {
            return StringUtils.isEmpty(data) || data.matches("[0-9]+");
    }
}
