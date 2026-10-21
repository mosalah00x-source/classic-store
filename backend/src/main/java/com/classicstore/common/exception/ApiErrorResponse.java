package com.classicstore.common.exception;

import java.util.Map;

public record ApiErrorResponse(
        String code,
        String messageAr,
        String messageEn,
        String traceId,
        Map<String, Object> details
) {
}
