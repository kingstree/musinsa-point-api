package musinsa.points.common.response;

import musinsa.points.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    // --- Success ---
    public static <T> ResponseEntity<ApiResult<T>> success(T data) {
        return ResponseEntity.ok(ApiResult.ofSuccess(data));
    }

    public static <T> ResponseEntity<ApiResult<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResult.ofSuccess(message, data));
    }

    // --- Failure (문자열 코드) ---
    public static <T> ResponseEntity<ApiResult<T>> failure(String message, String errorCode, HttpStatus status) {
        return new ResponseEntity<>(ApiResult.ofFailure(message, errorCode), status);
    }

    // --- Failure (Enum 코드) ---
    public static <T> ResponseEntity<ApiResult<T>> failure(ErrorCode errorCode) {
        return new ResponseEntity<>(ApiResult.ofFailure(errorCode), errorCode.getStatus());
    }

    public static <T> ResponseEntity<ApiResult<T>> failure(ErrorCode errorCode, String detailMessage) {
        return new ResponseEntity<>(ApiResult.ofFailure(errorCode, detailMessage), errorCode.getStatus());
    }

    // --- Exception 변환 (예상치 못한 예외용) ---
    public static <T> ResponseEntity<ApiResult<T>> exception(Exception e) {
        return new ResponseEntity<>(ApiResult.ofFailure(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()),
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}
