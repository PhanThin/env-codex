package vn.com.viettel.core.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({JasyptImportSelector.class})
public @interface EnableJasypt {
    String key() default "password";
}
