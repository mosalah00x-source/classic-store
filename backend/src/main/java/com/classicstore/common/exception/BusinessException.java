package com.classicstore.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

public class BusinessException extends RuntimeException {

    private final String code;
    private final String messageAr;
    private final String messageEn;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public BusinessException(String code, String messageAr, String messageEn) {
        this(code, messageAr, messageEn, HttpStatus.UNPROCESSABLE_ENTITY, Collections.emptyMap());
    }

    public BusinessException(String code, String messageAr, String messageEn, HttpStatus status) {
        this(code, messageAr, messageEn, status, Collections.emptyMap());
    }

    public BusinessException(String code, String messageAr, String messageEn, Map<String, Object> details) {
        this(code, messageAr, messageEn, HttpStatus.UNPROCESSABLE_ENTITY, details);
    }

    public BusinessException(String code, String messageAr, String messageEn, HttpStatus status, Map<String, Object> details) {
        super(messageEn);
        this.code = code;
        this.messageAr = messageAr;
        this.messageEn = messageEn;
        this.status = status != null ? status : HttpStatus.UNPROCESSABLE_ENTITY;
        this.details = details != null ? details : Collections.emptyMap();
    }

    public String getCode() {
        return code;
    }

    public String getMessageAr() {
        return messageAr;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
