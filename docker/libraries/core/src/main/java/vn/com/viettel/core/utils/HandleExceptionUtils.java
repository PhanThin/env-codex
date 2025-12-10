package vn.com.viettel.core.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import vn.com.viettel.core.config.I18n;
import vn.com.viettel.core.dto.response.BaseResponse;

import java.util.*;

public class HandleExceptionUtils {
    public static ResponseEntity<Object> errorResponse(String message, String path, HttpStatus httpStatus) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(httpStatus.value());
        baseResponse.setMessage(message);
        baseResponse.setPath(path);
        baseResponse.setTimestamp(new Date(System.currentTimeMillis()));
        baseResponse.setStatus(httpStatus.value());
        return new ResponseEntity<>(baseResponse, httpStatus);
    }

    public static String getMsgValidateException(ConstraintViolationException e) {
        String message = "", fieldName = "";
        Map<String, Object> attr = null;
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            message = violation.getMessage();
            Path.Node node = findLastNonEmptyPathNode(violation.getPropertyPath());
            if (node != null) fieldName = I18n.getMessage(node.getName());
            String constraintName = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            switch (Objects.requireNonNull(constraintName)) {
                case "NotBlank":
                case "NotEmpty":
                case "NotNull":
                    message = String.format(I18n.getMessage("msg.common.validate.not.null"), fieldName);
                    break;
                case "Size":
                    attr = violation.getConstraintDescriptor().getAttributes();
                    if (!CollectionUtils.isEmpty(attr)) {
                        int min = (int) attr.get("min");
                        int max = (int) attr.get("max");
                        message = String.format(I18n.getMessage("msg.common.validate.size.invalid"), fieldName, min, max);
                    }
                    break;
                case "Max":
                    attr = violation.getConstraintDescriptor().getAttributes();
                    if (!CollectionUtils.isEmpty(attr)) {
                        long max = (long) attr.get("value");
                        message = String.format(I18n.getMessage("msg.common.validate.max.invalid"), fieldName, max);
                    }
                    break;
                case "Min":
                    attr = violation.getConstraintDescriptor().getAttributes();
                    if (!CollectionUtils.isEmpty(attr)) {
                        long min = (long) attr.get("value");
                        message = String.format(I18n.getMessage("msg.common.validate.min.invalid"), fieldName, min);
                    }
                    break;
                case "Pattern":
                    message = String.format(I18n.getMessage("msg.common.validate.type.invalid"), fieldName);
                    break;
                case "Digits":
                    message = String.format(I18n.getMessage("msg.common.method.argument.not.valid"), fieldName);
            }
            break;
        }
        return message;
    }

    public static String getMsgMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = "";
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            message = error.getDefaultMessage();
            switch (Objects.requireNonNull(error.getCode())) {
                case "NotBlank":
                case "NotEmpty":
                case "NotNull":
                    message = String.format(I18n.getMessage("msg.common.validate.not.null"), I18n.getMessage(error.getField()));
                    break;
                case "Pattern":
                    message = String.format(I18n.getMessage("msg.common.validate.type.invalid"), I18n.getMessage(error.getField()));
                    break;
                case "Size":
                    if (error.getArguments() != null && error.getArguments().length == 3) {
                        int min = (int) error.getArguments()[2];
                        int max = (int) error.getArguments()[1];
                        message = String.format(I18n.getMessage("msg.common.validate.size.invalid"), I18n.getMessage(error.getField()), min, max);
                    }
                    break;
                case "Max":
                    if (error.getArguments() != null && error.getArguments().length == 2) {
                        long max = (long) error.getArguments()[1];
                        message = String.format(I18n.getMessage("msg.common.validate.max.invalid"), I18n.getMessage(error.getField()), max);
                    }
                    break;
                case "Min":
                    if (error.getArguments() != null && error.getArguments().length == 2) {
                        long min = (long) error.getArguments()[1];
                        message = String.format(I18n.getMessage("msg.common.validate.min.invalid"), I18n.getMessage(error.getField()), min);
                    }
                    break;
                case "Digits":
                    message = String.format(I18n.getMessage("msg.common.method.argument.not.valid"), I18n.getMessage(error.getField()));
            }
            break;
        }
        return message;
    }

    public static Path.Node findLastNonEmptyPathNode(Path path) {

        List<Path.Node> list = new ArrayList<>();
        for (Path.Node value : path) {
            list.add(value);
        }
        Collections.reverse(list);
        for (Path.Node node : list) {
            if (!StringUtils.isEmpty(node.getName())) {
                return node;
            }
        }
        return null;
    }
}
