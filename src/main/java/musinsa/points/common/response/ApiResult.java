package musinsa.points.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import musinsa.points.common.exception.ErrorCode;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;

    private ApiResult(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    public static <T> ApiResult<T> ofSuccess(T data){
        return new ApiResult<>(true, "Request successful", data, null);
    }

    public static <T> ApiResult<T> ofSuccess(String message) {
        return new ApiResult<>(true, message, null, null);
    }

    public static <T> ApiResult<T> ofSuccess(String message, T data) {
        return new ApiResult<>(true, message, data, null);
    }

    public static <T> ApiResult<T> ofFailure(String message, String errorCode) {
        return new ApiResult<>(false, message, null, errorCode);
    }

    // Enum 기반 실패
    public static <T> ApiResult<T> ofFailure(ErrorCode errorCode) {
        return new ApiResult<>(false, errorCode.getMessage(), null, errorCode.name());
    }

    public static <T> ApiResult<T> ofFailure(ErrorCode errorCode, String detailMessage) {
        String msg = detailMessage != null ? detailMessage : errorCode.getMessage();
        return new ApiResult<>(false, msg, null, errorCode.name());
    }
}
