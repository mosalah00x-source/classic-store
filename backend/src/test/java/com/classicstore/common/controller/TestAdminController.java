package com.classicstore.common.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/admin")
public class TestAdminController {

    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Boolean> ping() {
        return Map.of("ok", true);
    }
}
