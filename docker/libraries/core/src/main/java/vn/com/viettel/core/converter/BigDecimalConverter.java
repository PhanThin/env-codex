package vn.com.viettel.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BigDecimalConverter implements Converter<String, BigDecimal> {

    @Override
    public BigDecimal convert(String source) {
        String value = source;
        value = value.replaceAll("[^0-9.\\-]", "");
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }
}
