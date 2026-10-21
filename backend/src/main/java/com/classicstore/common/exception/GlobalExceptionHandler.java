package com.classicstore.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        String traceId = getTraceId();
        ApiErrorResponse response = new ApiErrorResponse(
                ex.getCode(),
                ex.getMessageAr(),
                ex.getMessageEn(),
                traceId,
                ex.getDetails()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        Map<String, Object> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ApiErrorResponse response = new ApiErrorResponse(
                "VALIDATION_ERROR",
                "خطأ في البيانات المدخلة",
                "Validation error",
                traceId,
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String traceId = getTraceId();
        ApiErrorResponse response = new ApiErrorResponse(
                "UNAUTHENTICATED",
                "غير مصرح بالدخول",
                "Unauthenticated access",
                traceId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = getTraceId();
        ApiErrorResponse response = new ApiErrorResponse(
                "FORBIDDEN",
                "غير مسموح بإجراء هذه العملية",
                "Access denied",
                traceId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoResourceFoundException ex) {
        String traceId = getTraceId();
        ApiErrorResponse response = new ApiErrorResponse(
                "NOT_FOUND",
                "العنصر المطلوب غير موجود",
                "Requested resource not found",
                traceId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception captured in GlobalExceptionHandler", ex);
        String traceId = getTraceId();
        ApiErrorResponse response = new ApiErrorResponse(
                "INTERNAL_ERROR",
                "حدث خطأ داخلي في النظام",
                "An internal error occurred",
                traceId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "";
    }
}
