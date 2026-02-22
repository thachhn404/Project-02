package com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    /**
     * Xử lý lỗi ResponseStatusException (lỗi nghiệp vụ ném ra từ Service).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason());
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    /**
     * Xử lý lỗi sai thông tin đăng nhập.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Email hoặc mật khẩu không chính xác");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Access Denied");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Không tìm thấy endpoint: " + ex.getResourcePath());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String message = coordinateMessageOrDefault(fieldErrors, joinFieldErrors(fieldErrors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Void>builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message(message.isBlank() ? ErrorCode.INVALID_REQUEST.getMessage() : message)
                .build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String message = coordinateMessageOrDefault(fieldErrors, joinFieldErrors(fieldErrors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Void>builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message(message.isBlank() ? ErrorCode.INVALID_REQUEST.getMessage() : message)
                .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return handleNumericOverflowIfPresent(ex);
    }

    @ExceptionHandler({JpaSystemException.class, DataException.class, DataAccessException.class})
    public ResponseEntity<ApiResponse<Void>> handleDatabaseExceptions(Exception ex) {
        return handleNumericOverflowIfPresent(ex);
    }

    /**
     * Xử lý các lỗi hệ thống không mong muốn khác.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Đã xảy ra lỗi hệ thống: " + ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String joinFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private static String coordinateMessageOrDefault(List<FieldError> fieldErrors, String defaultMessage) {
        boolean hasLatitudeError = fieldErrors.stream().anyMatch(e -> "latitude".equals(e.getField()));
        boolean hasLongitudeError = fieldErrors.stream().anyMatch(e -> "longitude".equals(e.getField()));
        if (hasLatitudeError || hasLongitudeError) {
            return "Tọa độ không hợp lệ. Vĩ độ [-90, 90], kinh độ [-180, 180].";
        }
        return defaultMessage;
    }

    private static String getRootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() == null ? "" : current.getMessage();
    }

    private static ResponseEntity<ApiResponse<Void>> handleNumericOverflowIfPresent(Throwable ex) {
        String rootMessage = getRootMessage(ex).toLowerCase();
        if (rootMessage.contains("arithmetic overflow") && rootMessage.contains("numeric")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Void>builder()
                    .code(ErrorCode.INVALID_REQUEST.getCode())
                    .message("Tọa độ không hợp lệ. Vui lòng kiểm tra lại vĩ độ/kinh độ.")
                    .build());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Void>builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message("Đã xảy ra lỗi hệ thống")
                .build());
    }
}
