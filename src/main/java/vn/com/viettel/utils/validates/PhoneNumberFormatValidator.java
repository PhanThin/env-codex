package vn.com.viettel.utils.validates;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;


public class PhoneNumberFormatValidator implements ConstraintValidator<PhoneNumberFormat, String> {
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isEmpty(phoneNumber) || phoneNumber.matches("^(84|0)([0-9]{9})");
    }
}
