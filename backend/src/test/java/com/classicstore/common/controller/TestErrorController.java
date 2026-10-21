package com.classicstore.common.controller;

import com.classicstore.common.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestErrorController {

    @GetMapping("/business-error")
    public void throwBusinessError() {
        throw new BusinessException(
                "BUSINESS_RULE_VIOLATION",
                "خطأ في تنفيذ العملية",
                "Business rule violation occurred",
                Map.of("reason", "invalid_state")
        );
    }

    @GetMapping("/ok")
    public String ok() {
        return "OK";
    }
}
