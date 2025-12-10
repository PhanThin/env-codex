package vn.com.viettel.utils.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.com.viettel.auth.utils.SecurityContextUtils;
import vn.com.viettel.core.config.I18n;
import vn.com.viettel.core.dto.response.BaseResponse;
import vn.com.viettel.core.utils.HandleExceptionUtils;
import vn.com.viettel.utils.ErrorApp;

import java.util.Date;
import java.util.Objects;

import static vn.com.viettel.core.utils.HandleExceptionUtils.getMsgMethodArgumentNotValid;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static ResponseEntity<Object> errorResponse(ErrorApp errorApp, String path, HttpStatus httpStatus) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(errorApp.getCode());
        baseResponse.setMessage(errorApp.getDescription());
        baseResponse.setPath(path);
        baseResponse.setTimestamp(new Date(System.currentTimeMillis()));
        baseResponse.setStatus(httpStatus.value());
        return new ResponseEntity<>(baseResponse, httpStatus);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleValidateException(ConstraintViolationException e, HttpServletRequest request) {
        LOGGER.error("Has ERROR handleValidateException: ", e);
        return HandleExceptionUtils.errorResponse(HandleExceptionUtils.getMsgValidateException(e), request.getRequestURI(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest req) {
        LOGGER.error("Has Access is denied ERROR user: {} in: {}", SecurityContextUtils.getUsername(), req.getRequestURI());
        return HandleExceptionUtils.errorResponse(ErrorApp.FORBIDDEN.getDescription(), req.getRequestURI(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex, HttpServletRequest request) {
        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            LOGGER.error("Has ERROR CustomException with message = {}, at = {}", ex.getMessage(), ex.getStackTrace()[0].toString());
        } else {
            LOGGER.error("Has ERROR CustomException with code = {}, message = {}", ex.getMessage(), ex.getMessage());
        }
        if (Objects.isNull(ex.getErrorApp()) && Objects.nonNull(ex.getCodeError())) {
            return errorResponse(ex.getErrorApp(), request.getRequestURI(), HttpStatus.BAD_REQUEST);
        }
        return HandleExceptionUtils.errorResponse(I18n.getMessage(ex.getMessage()), request.getRequestURI(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {
        LOGGER.error("Has ERROR handleException: ", ex);
        return HandleExceptionUtils.errorResponse(ex.getMessage(), request.getRequestURI(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        LOGGER.error("Has ERROR handleExceptionInternal: ", ex);
        if (ex instanceof MethodArgumentNotValidException e) {
            return HandleExceptionUtils.errorResponse(getMsgMethodArgumentNotValid(e), ((ServletWebRequest) request).getRequest().getRequestURI(), HttpStatus.BAD_REQUEST);
        }
        return HandleExceptionUtils.errorResponse(ex.getMessage(), ((ServletWebRequest) request).getRequest().getRequestURI(), HttpStatus.valueOf(statusCode.value()));
    }
}
