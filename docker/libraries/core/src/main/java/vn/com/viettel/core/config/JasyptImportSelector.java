package vn.com.viettel.core.config;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

@NoArgsConstructor
public class JasyptImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableJasypt.class.getName(), true);
        if (attributes != null) {
            String key = (String) attributes.get("key");
            System.setProperty("viettel-jasypt-pwd", key);
        }
        return new String[0];
    }
}
