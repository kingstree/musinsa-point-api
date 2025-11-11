package musinsa.points.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import musinsa.points.common.log.dto.ErrorLog;
import musinsa.points.common.log.dto.LogType;
import musinsa.points.common.log.util.StackTraceUtil;
import musinsa.points.common.response.ApiResult;
import musinsa.points.common.response.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger ERR_LOG = LoggerFactory.getLogger("api.error");


    private void logError(int status, String customCode, String message, Exception ex) {
        ErrorLog e = new ErrorLog();
        e.setLogType(LogType.ERROR);
        e.setStatus(status);
        e.setMessage(message);
        e.setType(ex != null ? ex.getClass().getName() : null);
        e.setCustomCode(customCode);
        e.setStackTrace(StackTraceUtil.toTrimmedString(ex));
        try {
            ERR_LOG.error("{}", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(e));
        } catch (Exception ignore) {
            ERR_LOG.error("[error-log] status={} code={} msg={}", status, customCode, message, ex);
        }
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        ErrorCode code = ex.getErrorCode();
        logError(code.getStatus().value(), code.name(), ex.getMessage(), ex);
        return ResponseUtil.failure(code, ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse(ErrorCode.VALIDATION_FAILED.getMessage());
        logError(ErrorCode.VALIDATION_FAILED.getStatus().value(), ErrorCode.VALIDATION_FAILED.name(), msg, ex);
        return ResponseUtil.failure(ErrorCode.VALIDATION_FAILED, msg);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse(ErrorCode.VALIDATION_FAILED.getMessage());
        logError(ErrorCode.VALIDATION_FAILED.getStatus().value(), ErrorCode.VALIDATION_FAILED.name(), msg, ex);
        return ResponseUtil.failure(ErrorCode.VALIDATION_FAILED, msg);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleEtc(Exception ex) {
        logError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_SERVER_ERROR.name(), ex.getMessage(), ex);
        return ResponseUtil.exception(ex);
    }
}
